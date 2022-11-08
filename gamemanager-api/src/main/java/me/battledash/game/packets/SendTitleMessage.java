package me.battledash.game.packets;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import net.battledash.sider.messages.SiderMessage;

import java.time.Duration;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class SendTitleMessage extends SiderMessage {

    private String title;
    private String subtitle;
    private long fadeIn;
    private long stay;
    private long fadeOut;

    private UUID[] recipients;

    public SendTitleMessage(String title, String subtitle, Duration fadeIn, Duration stay, Duration fadeOut,
                            UUID... recipients) {
        this.title = title;
        this.subtitle = subtitle;
        this.fadeIn = fadeIn.toMillis();
        this.stay = stay.toMillis();
        this.fadeOut = fadeOut.toMillis();
        this.recipients = recipients;
    }

    public SendTitleMessage(String title, String subtitle, UUID... recipients) {
        this(title, subtitle, Duration.ZERO, Duration.ofSeconds(1), Duration.ZERO, recipients);
    }

    public SendTitleMessage(String title, UUID... recipients) {
        this(title, null, recipients);
    }

}
