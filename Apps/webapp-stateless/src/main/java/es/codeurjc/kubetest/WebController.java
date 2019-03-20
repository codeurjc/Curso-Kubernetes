package es.codeurjc.kubetest;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Random;

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

	private String[] colors = {
			"FF0000",
			"FFA500",
			"FFFF00",
			"808000",
			"008000",
			"FF00FF",
			"00FF00",
			"008080",
			"00FFFF",
			"0000FF",
			"C0C0C0"
	};
	
	private int nextColor = new Random().nextInt(colors.length);
		
	@GetMapping("/")
	public String mainPage(HttpSession session, Model model) throws UnknownHostException {
		
		String userColor;
		if(session.isNew()) {
			userColor = colors[nextColor];
			nextColor = (nextColor + 1) % colors.length;
			session.setAttribute("userColor", userColor);
		} else {
			userColor = (String) session.getAttribute("userColor");
		}
		
		model.addAttribute("userColor", userColor);
		model.addAttribute("ip", InetAddress.getLocalHost().getHostAddress());
		model.addAttribute("random", new Random().nextInt());
		
		return "index";
	}	
}
