package top.publicnote.eldermoodai.backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.publicnote.eldermoodai.backend.enums.Gender;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateElderRequest {

    @Size(min = 1, max = 50, message = "姓名长度必须在1-50字符之间")
    private String name;

    private Gender gender;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    @Size(max = 500, message = "健康状况描述不能超过500字符")
    private String healthStatus;

    private Boolean privacyEnabled;
}
