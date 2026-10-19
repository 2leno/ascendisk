package api.poja.app.service;

import api.poja.app.mapper.SignalMapper;
import jakarta.annotation.PreDestroy;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Component
public class SignalStreamService {

  private static final long STREAM_TIMEOUT_MS = 30_000L;
  private static final long PUSH_INTERVAL_MS = 3_000L;

  private final SignalService signalService;
  private final SignalMapper signalMapper;
  private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

  public SignalStreamService(SignalService signalService, SignalMapper signalMapper) {
    this.signalService = signalService;
    this.signalMapper = signalMapper;
  }

  public SseEmitter stream() {
    var emitter = new SseEmitter(STREAM_TIMEOUT_MS);
    var task =
        scheduler.scheduleAtFixedRate(
            () -> push(emitter), 0, PUSH_INTERVAL_MS, TimeUnit.MILLISECONDS);
    emitter.onCompletion(() -> task.cancel(true));
    emitter.onTimeout(() -> task.cancel(true));
    return emitter;
  }

  private void push(SseEmitter emitter) {
    try {
      var signals = signalService.currentSignals().stream().map(signalMapper::toResponse).toList();
      emitter.send(SseEmitter.event().name("signals").data(signals, MediaType.APPLICATION_JSON));
    } catch (Exception e) {
      emitter.completeWithError(e);
    }
  }

  @PreDestroy
  void shutdown() {
    scheduler.shutdownNow();
  }
}
