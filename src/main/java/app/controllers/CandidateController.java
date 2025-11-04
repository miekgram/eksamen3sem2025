package app.controllers;

import app.daos.CandidateDAO;
import app.daos.SkillDAO;
import app.dtos.CandidateDTO;
import app.dtos.CandidateRequestDTO;
import app.dtos.SkillDTO;
import app.entities.Candidate;
import app.entities.CandidateSkill;
import app.entities.Skill;
import app.entities.SkillStatsApiResponse;
import app.exceptions.ApiException;
import app.services.Converters;
import app.services.SkillService;
import io.javalin.http.Handler;
import io.javalin.http.HttpStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class CandidateController implements IController {

    private static final Logger log = LoggerFactory.getLogger(CandidateController.class);

    private final CandidateDAO candidateDAO;
    private final SkillDAO skillDAO;
    private final EntityManagerFactory emf;
    private final SkillService skillService = new SkillService();
    private final Converters converter = new Converters();

    public CandidateController(EntityManagerFactory emf) {
        this.candidateDAO = new CandidateDAO(emf);
        this.skillDAO = new SkillDAO(emf);
        this.emf = emf;
    }

    // US-4: GET /candidates?category=FRONTEND
    @Override
    public Handler getAll() {
        return ctx -> {
            String category = ctx.queryParam("category");
            log.info("GET /candidates (category={})", category);

            List<Candidate> list = candidateDAO.getAll();

            if (category != null && !category.isBlank()) {
                Skill.Category cat;
                try {
                    cat = Skill.Category.valueOf(category.trim().toUpperCase());
                } catch (IllegalArgumentException e) {
                    log.warn("Unknown category requested: {}", category);
                    throw new ApiException(400, "Unknown category: " + category);
                }

                list = list.stream()
                        .filter(c -> c.getSkills() != null && c.getSkills().stream()
                                .anyMatch(cs -> cs != null && cs.getSkill() != null && cs.getSkill().getCategory() == cat))
                        .collect(Collectors.toList());
            }

            List<CandidateDTO> dtos = list.stream()
                    .map(converter::convertCandidateToCandidateDTO)
                    .collect(Collectors.toList());

            log.info("GET /candidates -> {} result(s)", dtos.size());
            ctx.status(HttpStatus.OK).json(dtos);
        };
    }

    // US-5: GET /candidates/{id} (inkl. enrichment)
    @Override
    public Handler getById() {
        return ctx -> {
            Integer id = Integer.parseInt(ctx.pathParam("id"));
            if (id <= 0) throw new ApiException(400, "id must be > 0");
            log.info("GET /candidates/{}", id);

            Candidate entity = candidateDAO.getById(id);
            if (entity == null) {
                log.warn("Candidate {} not found", id);
                throw new ApiException(404, "Candidate " + id + " not found");
            }

            CandidateDTO dto = converter.convertCandidateToCandidateDTO(entity);

            // Saml slugs
            List<String> slugs = new ArrayList<>();
            if (dto.getSkills() != null) {
                for (SkillDTO s : dto.getSkills()) {
                    if (s.getSlug() != null && !s.getSlug().isBlank()) slugs.add(s.getSlug());
                }
            }
            log.debug("Enrichment slugs for candidate {}: {}", id, slugs);

            // Fetch market data
            List<SkillStatsApiResponse.SkillData> stats = skillService.getStatsBySlugs(slugs);
            log.debug("Skill stats fetched for candidate {}: {} item(s)", id, stats.size());

            // Match på slug og sæt enrichment
            if (dto.getSkills() != null) {
                int enriched = 0;
                for (SkillDTO s : dto.getSkills()) {
                    if (s.getSlug() == null) continue;
                    for (SkillStatsApiResponse.SkillData st : stats) {
                        if (st.getSlug() != null && st.getSlug().equalsIgnoreCase(s.getSlug())) {
                            s.setPopularityScore(st.getPopularityScore());
                            s.setAverageSalary(st.getAverageSalary());
                            enriched++;
                            break;
                        }
                    }
                }
                log.info("Enriched {} skill(s) for candidate {}", enriched, id);
            }

            ctx.status(HttpStatus.OK).json(dto);
        };
    }

    // US-3: POST /candidates
    @Override
    public Handler create() {
        return ctx -> {
            log.info("POST /candidates");
            CandidateRequestDTO body = ctx.bodyAsClass(CandidateRequestDTO.class);
            if (body.getName() == null || body.getName().isBlank()) throw new ApiException(400, "name is required");
            if (body.getPhone() == null || body.getPhone().isBlank()) throw new ApiException(400, "phone is required");
            if (body.getEducation() == null || body.getEducation().isBlank()) throw new ApiException(400, "education is required");

            Candidate entity = converter.convertCandidateRequestToCandidate(body);
            if (entity.getSkills() == null) entity.setSkills(new HashSet<>());

            Candidate created = candidateDAO.create(entity);
            log.info("created candidate id={}", created.getCandidateId());
            ctx.status(HttpStatus.CREATED).json(converter.convertCandidateToCandidateDTO(created));
        };
    }

    // US-3: PUT /candidates/{id}
    @Override
    public Handler update() {
        return ctx -> {
            Integer id = Integer.parseInt(ctx.pathParam("id"));
            if (id <= 0) throw new ApiException(400, "id must be > 0");
            log.info("PUT /candidates/{}", id);

            Candidate existing = candidateDAO.getById(id);
            if (existing == null) {
                log.warn("Candidate {} not found (update)", id);
                throw new ApiException(404, "Candidate " + id + " not found");
            }

            CandidateRequestDTO body = ctx.bodyAsClass(CandidateRequestDTO.class);
            if (body.getName() == null || body.getName().isBlank()) throw new ApiException(400, "name is required");
            if (body.getPhone() == null || body.getPhone().isBlank()) throw new ApiException(400, "phone is required");
            if (body.getEducation() == null || body.getEducation().isBlank()) throw new ApiException(400, "education is required");

            existing.setName(body.getName().trim());
            existing.setPhone(body.getPhone().trim());
            existing.setEducation(body.getEducation().trim());

            Candidate saved = candidateDAO.update(existing);
            log.info("updated candidate id={}", id);
            ctx.status(HttpStatus.OK).json(converter.convertCandidateToCandidateDTO(saved));
        };
    }

    // US-3: DELETE /candidates/{id}
    @Override
    public Handler delete() {
        return ctx -> {
            Integer id = Integer.parseInt(ctx.pathParam("id"));
            if (id <= 0) throw new ApiException(400, "id must be > 0");
            log.info("DELETE /candidates/{}", id);

            Candidate found = candidateDAO.getById(id);
            if (found == null) {
                log.warn("Candidate {} not found (delete)", id);
                throw new ApiException(404, "Candidate " + id + " not found");
            }

            if (found.getSkills() != null) {
                int before = found.getSkills().size();
                found.getSkills().clear();
                candidateDAO.update(found);
                log.debug("Cleared {} link(s) for candidate {}", before, id);
            }
            candidateDAO.delete(id);
            log.info("deleted candidate id={}", id);

            ctx.status(HttpStatus.OK).json(converter.convertCandidateToCandidateDTO(found));
        };
    }

    // EXTRA (US-3): PUT /candidates/{candidateId}/skills/{skillId}
    public Handler linkSkill() {
        return ctx -> {
            Integer candidateId = Integer.parseInt(ctx.pathParam("candidateId"));
            Integer skillId = Integer.parseInt(ctx.pathParam("skillId"));
            if (candidateId <= 0 || skillId <= 0) throw new ApiException(400, "ids must be > 0");

            log.info("PUT /candidates/{}/skills/{}", candidateId, skillId);

            Candidate candidate = candidateDAO.getById(candidateId);
            if (candidate == null) {
                log.warn("Candidate {} not found (link)", candidateId);
                throw new ApiException(404, "Candidate " + candidateId + " not found");
            }
            Skill skill = skillDAO.getById(skillId);
            if (skill == null) {
                log.warn("Skill {} not found (link)", skillId);
                throw new ApiException(404, "Skill " + skillId + " not found");
            }

            EntityManager em = emf.createEntityManager();
            try {
                em.getTransaction().begin();
                candidate = em.merge(candidate);
                skill = em.merge(skill);
                em.persist(CandidateSkill.builder().candidate(candidate).skill(skill).build());
                em.getTransaction().commit();
                log.info("linked skill {} to candidate {}", skillId, candidateId);
            } catch (Exception e) {
                if (em.getTransaction().isActive()) em.getTransaction().rollback();
                log.error("Failed to link skill {} to candidate {}: {}", skillId, candidateId, e.getMessage());
                throw new ApiException(500, "Failed to link skill to candidate");
            } finally {
                em.close();
            }

            Candidate updated = candidateDAO.getById(candidateId);
            ctx.status(HttpStatus.OK).json(converter.convertCandidateToCandidateDTO(updated));
        };
    }

    // EXTRA (US-6): GET /reports/candidates/top-by-popularity
    public Handler getTopByPopularity() {
        return ctx -> {
            log.info("GET /reports/candidates/top-by-popularity");

            List<Candidate> all = candidateDAO.getAll();
            Integer bestId = null; double bestAvg = -1.0;

            for (Candidate c : all) {
                List<String> slugs = new ArrayList<>();
                if (c.getSkills() != null) {
                    for (CandidateSkill cs : c.getSkills()) {
                        if (cs != null && cs.getSkill() != null &&
                                cs.getSkill().getSlug() != null && !cs.getSkill().getSlug().isBlank()) {
                            slugs.add(cs.getSkill().getSlug());
                        }
                    }
                }
                if (slugs.isEmpty()) continue;

                List<SkillStatsApiResponse.SkillData> stats = skillService.getStatsBySlugs(slugs);

                int sum = 0, count = 0;
                if (c.getSkills() != null) {
                    for (CandidateSkill cs : c.getSkills()) {
                        if (cs == null || cs.getSkill() == null || cs.getSkill().getSlug() == null) continue;
                        String slug = cs.getSkill().getSlug();

                        SkillStatsApiResponse.SkillData match = null;
                        for (SkillStatsApiResponse.SkillData st : stats) {
                            if (st.getSlug() != null && st.getSlug().equalsIgnoreCase(slug)) { match = st; break; }
                        }
                        if (match != null && match.getPopularityScore() != null) { sum += match.getPopularityScore(); count++; }
                    }
                }
                if (count == 0) continue;

                double avg = ((double) sum) / count;
                if (avg > bestAvg) { bestAvg = avg; bestId = c.getCandidateId(); }
            }

            log.info("Top-by-popularity -> candidateId={}, averagePopularity={}", bestId, (bestId == null ? null : bestAvg));
            ctx.status(HttpStatus.OK).json(java.util.Map.of(
                    "candidateId", bestId,
                    "averagePopularity", bestId == null ? null : bestAvg
            ));
        };
    }
}
