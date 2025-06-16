package tr.edu.metu.ceng.uno.user.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordRequest {

    @NotBlank(message = "New password is required")
    private String newPassword;

    @Override
    public String toString() {
        return "ResetPasswordRequest{" +
                "newPassword='[PROTECTED]'" + //keeping password safe
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResetPasswordRequest resetPasswordRequest = (ResetPasswordRequest) o;
        return newPassword.equals(resetPasswordRequest.newPassword);
    }

    @Override
    public int hashCode() {
        return Objects.hash(newPassword);
    }
}
