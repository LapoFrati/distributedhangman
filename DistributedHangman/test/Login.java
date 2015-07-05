import javax.swing.JOptionPane;

public class Login {
	public static void main(String[] args) {
		String user = JOptionPane.showInputDialog(null, "user");
		String password = JOptionPane.showInputDialog(null, "password");

		if ("edu4java".equals(user) && "myPassword".equals(password)) {
			JOptionPane.showMessageDialog(null, "login ok");
		}else {
			JOptionPane.showMessageDialog(null, "login failed");
		}
	}
}