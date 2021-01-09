package it.dantar.games.sse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
public class SseService {

	@Autowired
	ApplicationContext context;
	
	private static final Long TIMEOUT = 1000 * 60 * 60L; // 1 ora

	private Map<String, List<SseEmitter>> emitters = new HashMap<>();

	static final private Map<String, String> PING = new HashMap<String, String>();
	static {
		PING.put("code", "ping");
	}
	
	@Scheduled(fixedDelayString = "${service.ping.delay}")
	public void refreshSse() {
		this.emitters.keySet().stream()
		.forEach(k -> this.broadcastJson(k, PING));			
	}

	public void broadcastJson(String gameId, Object data) {
		this.emitters.get(gameId).forEach(emitter -> {
			try {
				Logger.getLogger(this.getClass().getName()).warning(String.format("Broadcast to SSE %s in game %s (%s).", 
						emitter.toString(), gameId, this.emitters.get(gameId).toString()));				
				emitter.send(SseEmitter.event().data(data, MediaType.APPLICATION_JSON));
			} catch (IOException e) {
				// emitter.completeWithError(e);
				Logger.getLogger(this.getClass().getName()).warning(String.format("\tERROR for SSE %s in game %s. Exception message: %s", 
						emitter.toString(), gameId, e.getMessage()));
			}
		});
	}

	public SseEmitter sse(String gameId) {
		Logger.getLogger(this.getClass().getName()).info(String.format("SSE request: %s", gameId));
		if (!emitters.containsKey(gameId)) {
			Logger.getLogger(this.getClass().getName()).info(String.format("\t game %s does not exist: create game", gameId));
			emitters.put(gameId, new ArrayList<>());
		}
		SseEmitter sse = new SseEmitter(TIMEOUT);
		sse.onError(error -> {
			Logger.getLogger(this.getClass().getName()).info(String.format("SSE error for game %s (%s): removing SSE %s %s", 
					gameId, this.emitters.get(gameId).toString(), this.emitters.get(gameId).indexOf(sse), sse.toString()));
			Boolean done = this.emitters.get(gameId).remove(sse);
			Logger.getLogger(this.getClass().getName()).info(String.format("\tremoval done: %s", done));
			sse.completeWithError(new RuntimeException("SSE client error"));
		});
		sse.onCompletion(() -> {
			Logger.getLogger(this.getClass().getName()).info(String.format("SSE completion in game %s (%s): SSE %s %s", 
					gameId, this.emitters.get(gameId).toString(), this.emitters.get(gameId).indexOf(sse), sse.toString()));
		});
		sse.onTimeout(() -> {
			Logger.getLogger(this.getClass().getName()).info(String.format("SSE timeout in game %s (%s): SSE %s %s", 
					gameId, this.emitters.get(gameId).toString(), this.emitters.get(gameId).indexOf(sse), sse.toString()));
		});
		emitters.get(gameId).add(sse);
		return sse;
	}

	public String generateGameId() {
		String result = null;
		Random random = new Random(new Date().getTime());
		while (result == null || this.emitters.containsKey(result)) {
			result = "";
			for (int i = 0; i < 6; i++) {
				int max = 10;
				int min = i==0?1:0;
				result = result + (random.nextInt(max - min) + min);
			}
		}
		this.emitters.put(result, new ArrayList<>());
		return result;
	}

}
