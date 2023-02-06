package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private final Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

    @Autowired
    private TelegramBot telegramBot;

    @Autowired
    private NotificationTaskRepository notificationTaskRepository;

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        updates.forEach(update -> {
            logger.info("Processing update: {}", update);
            // Process your updates here
            replyToNewUser(update);
            createNewTask(update);
        });
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    private void createNewTask(Update update) {
        String inputMessage = update.message().text();

        Pattern pattern = Pattern.compile("([0-9.:\\s]{16})(\\s)([\\W+]+)");
        Matcher matcher = pattern.matcher(inputMessage);

        if (matcher.matches()) {
            System.out.println("GOTCHA");
            long chatId = update.message().chat().id();
            String date = matcher.group(1);
            LocalDateTime formattedDate = LocalDateTime.parse(date, DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
            String task = matcher.group(3);
            System.out.println("Дата и время: " + date);
            System.out.println("Название задачи: " + task);
            NotificationTask newTask = new NotificationTask(chatId, task, formattedDate);
            notificationTaskRepository.save(newTask);
        }
    }

    private void replyToNewUser(Update update) {
        if (update.message().text().equals("/start")) {
            System.out.println(update.message().chat().id());
            long chatId = update.message().chat().id();
            String messageText = "Hello new User!";
            SendMessage message = new SendMessage(chatId, messageText);
            SendResponse response = telegramBot.execute(message);
        }
    }

}
