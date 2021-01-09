package it.dantar.games.sse;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@CrossOrigin
public class SseController {

	@Autowired
	SseService sseService;
	
	@GetMapping("/alive")
	public Boolean alive() {
		return true;
	}

	@PostMapping("/games/new")
	public Map<String, String> postNewGame() {
		Map<String, String> result = new HashMap<String, String>();
		result.put("game", sseService.generateGameId());
		return result;
	}
	
	@GetMapping("/games/{gameId}/sse")
	public SseEmitter playerSse(@PathVariable String gameId) {
		return sseService.sse(gameId);
	}

	@PostMapping("/games/{gameId}/json")
	public void postMessage(@PathVariable String gameId, @RequestBody Object message) {
		sseService.broadcastJson(gameId, message);
	}

}
