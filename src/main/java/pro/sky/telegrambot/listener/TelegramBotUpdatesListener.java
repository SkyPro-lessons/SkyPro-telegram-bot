package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.*;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
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
        if (update.message() == null || update.message().text() == null) {
            return;
        }
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

    private void replyToNewUser(Update update) throws NullPointerException {
        if (null != update.message() && null != update.message().text()) {
            if (update.message().text().equals("/start")) {
                System.out.println(update.message().chat().id());
                long chatId = update.message().chat().id();
                String messageText = "Hello new User!";
                SendMessage message = new SendMessage(chatId, messageText);
                SendResponse response = telegramBot.execute(message);
                setKeyboard();
            }
        }
    }

    @Scheduled(cron = "0 0/1 * * * *")
    public void findActualTasks() {
        LocalDateTime taskTime = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        List<NotificationTask> list = this.notificationTaskRepository.findNotificationTasksBySendTime(taskTime);
        if (!list.isEmpty()) {
            this.sendNotifications(list);
        }
    }

    private void sendNotifications(List<NotificationTask> notificationTaskList) {
        for (NotificationTask task : notificationTaskList) {
            String messageText = task.getSendMessage();
            Long chatId = task.getChatId();
            SendMessage message = new SendMessage(chatId, messageText);
            setButtons(message);
            SendResponse response = telegramBot.execute(message);
        }
    }

    public void setButtons(SendMessage sendMessage) {
        // Создаем клавиуатуру
        //ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        Keyboard keyboard = new ReplyKeyboardMarkup(
                new KeyboardButton[]{
                        new KeyboardButton("text"),
                        new KeyboardButton("contact").requestContact(true),
                        new KeyboardButton("location").requestLocation(true)
                }
        );

        InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup(
                new InlineKeyboardButton[]{
                        new InlineKeyboardButton("Кнопка 1").url("www.google.com"),
                        new InlineKeyboardButton("Кнопка 2").callbackData("callback_data"),
                        new InlineKeyboardButton("Кнопка 3").switchInlineQuery("Привет матушка")
                },
                new InlineKeyboardButton[]{
                        new InlineKeyboardButton("Кнопка 4").callbackData("syaska"),
                        new InlineKeyboardButton("Кнопка 5").callbackData("mosaska")
                }
                );

        sendMessage.replyMarkup(inlineKeyboard);

//        replyKeyboardMarkup.
        //sendMessage.setReplyMarkup(replyKeyboardMarkup);
        /*replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);*/

        // Создаем список строк клавиатуры
        //List<KeyboardRow> keyboard = new ArrayList<>();

        // Первая строчка клавиатуры
        //KeyboardRow keyboardFirstRow = new KeyboardRow();
        // Добавляем кнопки в первую строчку клавиатуры
        //keyboardFirstRow.add(new KeyboardButton(“Привет”));

        // Вторая строчка клавиатуры
        //KeyboardRow keyboardSecondRow = new KeyboardRow();
        // Добавляем кнопки во вторую строчку клавиатуры
        //keyboardSecondRow.add(new KeyboardButton(“Помощь”);

        // Добавляем все строчки клавиатуры в список
        //keyboard.add(keyboardFirstRow);
        //keyboard.add(keyboardSecondRow);
        // и устанваливаем этот список нашей клавиатуре
        //replyKeyboardMarkup.setKeyboard(keyboard);
    }

    public void setKeyboard() {
        Keyboard replyKeyboardMarkup = new ReplyKeyboardMarkup(
                new String[]{"first row button1", "first row button2"},
                new String[]{"second row button1", "second row button2"})
                .oneTimeKeyboard(true)   // optional
                .resizeKeyboard(true)    // optional
                .selective(true);        // optional

        Keyboard keyboard = new ReplyKeyboardMarkup(
                new KeyboardButton[]{
                        new KeyboardButton("text"),
                        new KeyboardButton("contact").requestContact(true),
                        new KeyboardButton("location").requestLocation(true)
                }
        );

        InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup(
                new InlineKeyboardButton[]{
                        new InlineKeyboardButton("url").url("www.google.com"),
                        new InlineKeyboardButton("callback_data").callbackData("callback_data"),
                        new InlineKeyboardButton("Switch!").switchInlineQuery("switch_inline_query")
                });
    }

}
