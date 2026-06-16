package top.publicnote.eldermoodai.backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
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
public class ElderRequest {

    @NotBlank(message = "姓名不能为空")
    @Size(min = 1, max = 50, message = "姓名长度必须在1-50字符之间")
    private String name;

    @NotNull(message = "性别不能为空")
    private Gender gender;

    @NotNull(message = "出生日期不能为空")
    @Past(message = "出生日期必须是过去的日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    @Size(max = 500, message = "健康状况描述不能超过500字符")
    private String healthStatus;

    private Boolean privacyEnabled;

    private Long version;
}
