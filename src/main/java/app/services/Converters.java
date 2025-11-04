package app.services;

import app.dtos.CandidateDTO;
import app.dtos.CandidateRequestDTO;
import app.dtos.SkillDTO;
import app.entities.CandidateSkill;
import app.entities.Skill;
import app.entities.Candidate;

import java.util.Set;
import java.util.stream.Collectors;

public class Converters {

    // ===================== DTO -> ENTITY =====================

    /** Brug ved POST /candidates (ren input-DTO) */
    public Candidate convertCandidateRequestToCandidate(CandidateRequestDTO dto){
        if (dto == null) return null;
        Candidate candidate = Candidate.builder()
                .name(dto.getName())
                .phone(dto.getPhone())
                .education(dto.getEducation())
                .build();
        return candidate;
    }

    /** Brug hvis du vil mappe en komplet CandidateDTO (inkl. skills) til entity */
    public Candidate convertCandidateDTOToCandidate(CandidateDTO dto){
        if (dto == null) return null;

        Candidate candidate = Candidate.builder()
                .candidateId(dto.getId())  //sæt id hvis til stede
                .name(dto.getName())
                .phone(dto.getPhone())
                .education(dto.getEducation())
                .build();

        // Map DTO.skills -> CandidateSkill(join) med dette candidate-objekt
        if (dto.getSkills() != null && !dto.getSkills().isEmpty()) {
            Set<CandidateSkill> links = dto.getSkills().stream()
                    .map(this::convertSkillDTOToSkill)
                    .map(skill -> CandidateSkill.builder()
                            .candidate(candidate)
                            .skill(skill)
                            .build())
                    .collect(Collectors.toSet());
            candidate.setSkills(links);
        }
        return candidate;
    }

    /** Simpel mapping fra SkillDTO -> Skill (brugbar ved fx admin-CRUD på skills) */
    public Skill convertSkillDTOToSkill(SkillDTO dto){
        if (dto == null) return null;
        return Skill.builder()
                .skillId(dto.getId())
                .name(dto.getName())
                .slug(dto.getSlug())
                .category(dto.getCategory())
                .description(dto.getDescription())
                .build();
    }


    // ===================== ENTITY -> DTO =====================

    // ===== ENTITY -> DTO (bruges i alle responses) =====
    public CandidateDTO convertCandidateToCandidateDTO(Candidate candidate) {
        if (candidate == null) return null;
        return CandidateDTO.builder()
                .id(candidate.getCandidateId())
                .name(candidate.getName())
                .phone(candidate.getPhone())
                .education(candidate.getEducation())
                .skills(convertSkillsOfCandidate(candidate))  // <- mapper join -> SkillDTO
                .build();
    }


    /** Hjælp: map join-entity -> Set<SkillDTO> */
    private Set<SkillDTO> convertSkillsOfCandidate(Candidate candidate){
        if (candidate.getSkills() == null) return Set.of();
        return candidate.getSkills().stream()
                .map(CandidateSkill::getSkill)
                .map(this::convertSkillToDTO)
                .collect(Collectors.toSet());
    }

    /** Entity -> DTO for Skill */
    public SkillDTO convertSkillToDTO(Skill s){
        if (s == null) return null;
        return SkillDTO.builder()
                .id(s.getSkillId())
                .name(s.getName())
                .slug(s.getSlug())
                .category(s.getCategory())
                .description(s.getDescription())
                // enrichment-felter (popularity/averageSalary) sættes i controlleren, hvis relevant
                .build();
    }
}




//    public Candidate convertTripDTOToTrip(SkillDTO tripDTO){
//        Candidate trip = Candidate.builder()
//                .name(tripDTO.getName())
//                .start(tripDTO.getStart())
//                .end(tripDTO.getEnd())
//                .locationCoordinates(tripDTO.getLocationCoordinates())
//                .price(tripDTO.getPrice())
//                .category(tripDTO.getCategory())
//                .guide(convertGuideDTOToGuide(tripDTO.getGuideDTO()))
//                .build();
//        return trip;
//    }
//
//    public Skill convertGuideDTOToGuide(CandidateDTO guideDTO){
//        if(guideDTO == null) return null;
//        Skill guide = Skill.builder()
//                .guideId(guideDTO.getGuideId())
//                .name(guideDTO.getName())
//                .email(guideDTO.getEmail())
//                .phone(guideDTO.getPhone())
//                .yearsOfExp(guideDTO.getYearsOfExp())
//                .trips(guideDTO.getTripDTOs().stream().map(tripDTO -> convertTripDTOToTrip(tripDTO)).collect(Collectors.toSet()))
//                .build();
//        return guide;
//    }
//


//    public User convertUserDtoToUser(UserCreateDTO userCreateDTO, EntityManagerFactory emf){
//        UserDAO userDAO = new UserDAO(emf);
//        User user = User.builder()
//                .address(userCreateDTO.getAddress())
//                .userName(userCreateDTO.getUserName())
//                .phone(userCreateDTO.getPhone())
//                .roles(userCreateDTO.getRoles().stream().map(role -> new Role(role.toUpperCase())).collect(Collectors.toSet()))
//                .password(userCreateDTO.getPassword())
//                .events(userCreateDTO.getEvents())
//                .build();
//
//        return user;
//    }
//
//    public Event convertEventDtoToEvent(EventDTO eventDTO, EntityManagerFactory emf){
//        EventDAO eventDAO = new EventDAO(emf);
//        Event event = Event.builder()
//                .category(eventDTO.getCategory())
//                .description(eventDTO.getDescription())
//                .max(eventDTO.getMax())
//                .price(eventDTO.getPrice())
//                .name(eventDTO.getName())
//                //.interrested(eventDTO.getInterrested())
//                .participants(eventDTO.getParticipants())
//                .localDateTime(eventDTO.getLocalDateTime())
//                .location(eventDTO.getLocation())
//                .build();
//        return event;
//    }


