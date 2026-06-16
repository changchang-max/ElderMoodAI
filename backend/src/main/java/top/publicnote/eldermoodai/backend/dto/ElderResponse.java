package top.publicnote.eldermoodai.backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.publicnote.eldermoodai.backend.enums.Gender;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ElderResponse {

    private Long id;

    private String name;

    private Gender gender;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    private String healthStatus;

    private Boolean privacyEnabled;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    public int getAge() {
        return birthDate == null ? 0 : Period.between(birthDate, LocalDate.now()).getYears();
    }
}
