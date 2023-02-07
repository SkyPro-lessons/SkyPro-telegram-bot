package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TelegramBotUpdatesListenerTests {

    @Mock
    private NotificationTaskRepository repositoryMock;

    @Mock
    private List<Update> updatesMock;

    @Test
    @Disabled
    public void shouldCallMethodsReplyToUserAndCrateNewTask() {
        when(updatesMock.get(0).message().chat().id());

        //verify(updatesMock, times());
    }

}
