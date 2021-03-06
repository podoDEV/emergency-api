package world.podo.travelable.infrastructure.firebase;

import com.google.firebase.messaging.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import world.podo.travelable.domain.PushException;
import world.podo.travelable.domain.PushRequest;
import world.podo.travelable.domain.PushService;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FirebasePushService implements PushService {
    @Override
    public void send(PushRequest pushRequest) {
        Assert.notNull(pushRequest, "'pushRequest' must not be null");

        Notification notification = Notification.builder()
                                                .setTitle(pushRequest.getTitle())
                                                .setBody(pushRequest.getBody())
                                                .build();

        // This registration token comes from the client FCM SDKs.
        // See documentation on defining a message payload.
        List<Message> messages = pushRequest.getRegistrationTokens().stream()
                                            .map(token -> Message.builder()
                                                                 .setNotification(notification)
                                                                 .setToken(token)
                                                                 .build())
                                            .collect(Collectors.toList());

        // Send a message to the device corresponding to the provided registration token.
        BatchResponse batchResponse = null;
        try {
            batchResponse = FirebaseMessaging.getInstance().sendAll(messages);
        } catch (FirebaseMessagingException ex) {
            log.error("Failed to send push message using firebase", ex);
            throw new PushException("Failed to send push message using firebase");
        }
        // Response is a message ID string.
        log.info("Successfully sent message: {}", batchResponse);
    }
}
