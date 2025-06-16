package tr.edu.metu.ceng.uno.template;

public class HtmlTemplates {

    public static String getPasswordResetForm(String token) {
        return "<!DOCTYPE html>" +
            "<html>" +
            "<head>" +
            "    <title>Reset Password</title>" +
            "    <style>" +
            "        body { font-family: Arial, sans-serif; margin: 0; padding: 20px; }" +
            "        .container { max-width: 500px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 5px; }" +
            "        h2 { color: #333; }" +
            "        .form-group { margin-bottom: 15px; }" +
            "        label { display: block; margin-bottom: 5px; }" +
            "        input[type='password'] { width: 100%; padding: 8px; border: 1px solid #ddd; border-radius: 4px; }" +
            "        button { background-color: #4CAF50; color: white; padding: 10px 15px; border: none; border-radius: 4px; cursor: pointer; }" +
            "        button:hover { background-color: #45a049; }" +
            "        .message { margin-top: 20px; padding: 10px; border-radius: 4px; }" +
            "        .success { background-color: #dff0d8; color: #3c763d; border: 1px solid #d6e9c6; }" +
            "        .error { background-color: #f2dede; color: #a94442; border: 1px solid #ebccd1; }" +
            "        .hidden { display: none; }" +
            "    </style>" +
            "    <script>" +
            "        function submitForm() {" +
            "            const password = document.getElementById('newPassword').value;" +
            "            const messageDiv = document.getElementById('message');" +
            "            const formElement = document.getElementById('resetForm');" +
            "            " +
            "            fetch('/user/reset-password?token=" + token + "', {" +
            "                method: 'POST'," +
            "                headers: { 'Content-Type': 'application/json' }," +
            "                body: JSON.stringify({ newPassword: password })" +
            "            })" +
            "            .then(response => {" +
            "                return response.text().then(text => {" +
            "                    if (response.ok) {" +
            "                        messageDiv.className = 'message success';" +
            "                        formElement.className = 'hidden';" +
            "                    } else {" +
            "                        messageDiv.className = 'message error';" +
            "                    }" +
            "                    messageDiv.textContent = text;" +
            "                    messageDiv.style.display = 'block';" +
            "                });" +
            "            })" +
            "            .catch(error => {" +
            "                messageDiv.className = 'message error';" +
            "                messageDiv.textContent = 'An error occurred: ' + error;" +
            "                messageDiv.style.display = 'block';" +
            "            });" +
            "            return false;" +
            "        }" +
            "    </script>" +
            "</head>" +
            "<body>" +
            "    <div class='container'>" +
            "        <h2>Reset Your Password</h2>" +
            "        <form id='resetForm' onsubmit='return submitForm();'>" +
            "            <div class='form-group'>" +
            "                <label for='newPassword'>New Password:</label>" +
            "                <input type='password' id='newPassword' required>" +
            "            </div>" +
            "            <button type='submit'>Reset Password</button>" +
            "        </form>" +
            "        <div id='message' class='message hidden'></div>" +
            "    </div>" +
            "</body>" +
            "</html>";
    }
}
