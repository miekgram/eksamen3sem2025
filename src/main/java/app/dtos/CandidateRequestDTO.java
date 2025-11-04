package app.dtos;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
@Builder
public class CandidateRequestDTO {
    private String name;
    private String phone;
    private String education;
}

