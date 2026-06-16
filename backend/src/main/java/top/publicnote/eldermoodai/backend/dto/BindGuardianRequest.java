package top.publicnote.eldermoodai.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BindGuardianRequest {

    @NotNull(message = "老人ID不能为空")
    private Long elderId;

    @NotNull(message = "监护人ID不能为空")
    private Long guardianId;
}
