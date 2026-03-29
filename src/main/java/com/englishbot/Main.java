package com.englishbot;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Main extends TelegramLongPollingBot {

    private static final String BOT_TOKEN = "";
    private static final String BOT_USERNAME = "";


    private Database db;
    private Map<Long, String> trainingSessions;

    public Main() {
        db = new Database();
        trainingSessions = new ConcurrentHashMap<>();
    }

    @Override
    public String getBotToken() {
        return BOT_TOKEN;
    }

    @Override
    public String getBotUsername() {
        return BOT_USERNAME;
    }

    // Создание клавиатуры с кнопками
    private ReplyKeyboardMarkup createKeyboard() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboard = new ArrayList<>();

        // Первый ряд
        KeyboardRow row1 = new KeyboardRow();
        row1.add("🌟 Слово дня");
        row1.add("🎲 Случайное слово");

        // Второй ряд
        KeyboardRow row2 = new KeyboardRow();
        row2.add("📖 Словарь");
        row2.add("📝 Мои слова");

        // Третий ряд
        KeyboardRow row3 = new KeyboardRow();
        row3.add("➕ Добавить слово");
        row3.add("🎯 Тренировка");

        // Четвертый ряд
        KeyboardRow row4 = new KeyboardRow();
        row4.add("📊 Статистика");

        keyboard.add(row1);
        keyboard.add(row2);
        keyboard.add(row3);
        keyboard.add(row4);

        keyboardMarkup.setKeyboard(keyboard);
        return keyboardMarkup;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            long userId = update.getMessage().getFrom().getId();
            String userName = update.getMessage().getFrom().getFirstName();
            String username = update.getMessage().getFrom().getUserName();

            // Проверяем, новый ли пользователь
            boolean isNewUser = !db.userExists(userId);

            // Регистрируем пользователя (если новый)
            db.registerUser(userId, username, userName);

            SendMessage message = new SendMessage();
            message.setChatId(chatId);
            message.setParseMode("Markdown");

            // Обработка нажатий на кнопки
            if (messageText.equals("🌟 Слово дня")) {
                messageText = "/wordoftheday";
            }
            else if (messageText.equals("🎲 Случайное слово")) {
                messageText = "/random";
            }
            else if (messageText.equals("📖 Словарь")) {
                messageText = "/words";
            }
            else if (messageText.equals("📝 Мои слова")) {
                messageText = "/mywords";
            }
            else if (messageText.equals("➕ Добавить слово")) {
                message.setText("➕ *Добавь новое слово*\n\n" +
                        "Введи команду:\n" +
                        "`/add слово - перевод`\n\n" +
                        "Пример: `/add cat - кошка`");
                try {
                    execute(message);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
                return;
            }
            else if (messageText.equals("🎯 Тренировка")) {
                messageText = "/train";
            }
            else if (messageText.equals("📊 Статистика")) {
                messageText = "/stats";
            }

            // Проверяем, находится ли пользователь в режиме тренировки
            if (trainingSessions.containsKey(userId)) {
                String currentWord = trainingSessions.get(userId);
                String expectedTranslation = db.getTranslation(userId, currentWord);

                if (messageText.equalsIgnoreCase(expectedTranslation)) {
                    message.setText("✅ *Правильно!* Молодец! 🎉\n\n" +
                            "Хочешь продолжить? Нажми кнопку 🎯 Тренировка");
                    db.updateWordStats(userId, currentWord, true);
                    trainingSessions.remove(userId);
                    message.setReplyMarkup(createKeyboard());
                } else if (!messageText.startsWith("/") &&
                        !messageText.equals("🌟 Слово дня") &&
                        !messageText.equals("🎲 Случайное слово") &&
                        !messageText.equals("📖 Словарь") &&
                        !messageText.equals("📝 Мои слова") &&
                        !messageText.equals("➕ Добавить слово") &&
                        !messageText.equals("🎯 Тренировка") &&
                        !messageText.equals("📊 Статистика")) {
                    message.setText("❌ *Неправильно.*\n\n" +
                            "Правильный перевод: *" + expectedTranslation + "*\n\n" +
                            "Попробуй еще раз! Нажми кнопку 🎯 Тренировка");
                    db.updateWordStats(userId, currentWord, false);
                    trainingSessions.remove(userId);
                    message.setReplyMarkup(createKeyboard());
                } else {
                    trainingSessions.remove(userId);
                }
            }

            // Обрабатываем команды
            if (messageText.equals("/start")) {
                message.setText("Привет, " + userName + "! 👋\n\n" +
                        "Я бот для изучения английского языка!\n\n" +
                        "🌟 *Слово дня* — красивое слово с историей\n" +
                        "🎲 *Случайное слово* — расширяй словарь\n" +
                        "📖 *Словарь* — все слова с примерами\n\n" +
                        "👇 *Нажми на кнопку ниже*");
                message.setReplyMarkup(createKeyboard());
            }
            else if (messageText.equals("/wordoftheday")) {
                Dictionary.WordEntry wordOfDay = Dictionary.getWordOfTheDay();
                message.setText(wordOfDay.formatWordOfTheDay());
                message.setReplyMarkup(createKeyboard());
            }
            else if (messageText.equals("/stats")) {
                String stats = db.getUserStats(userId);
                message.setText(stats);
                message.setReplyMarkup(createKeyboard());
            }
            else if (messageText.equals("/mywords")) {
                List<Map<String, String>> words = db.getUserWords(userId);
                if (words.isEmpty()) {
                    message.setText("📭 *У тебя пока нет своих слов.*\n\n" +
                            "Вот встроенные слова: нажми кнопку 📖 Словарь\n\n" +
                            "Добавь свое слово: кнопка ➕ Добавить слово");
                } else {
                    StringBuilder sb = new StringBuilder("📝 *Мои слова* (" + words.size() + " слов)\n\n");
                    int count = 0;
                    for (Map<String, String> word : words) {
                        sb.append("🔹 *").append(word.get("word")).append("* — ").append(word.get("translation"));
                        sb.append(" ✅").append(word.get("correct")).append(" ❌").append(word.get("wrong")).append("\n");
                        count++;
                        if (count >= 15) {
                            sb.append("\n... и еще ").append(words.size() - 15).append(" слов");
                            break;
                        }
                    }
                    message.setText(sb.toString());
                }
                message.setReplyMarkup(createKeyboard());
            }
            else if (messageText.equals("/words")) {
                StringBuilder sb = new StringBuilder("📚 *Словарь* (" + Dictionary.size() + " слов)\n\n");
                int count = 0;
                for (Dictionary.WordEntry entry : Dictionary.getAllWords().values()) {
                    sb.append(entry.formatShort()).append("\n");
                    count++;
                    if (count >= 15) {
                        sb.append("\n... и еще ").append(Dictionary.size() - 15).append(" слов\n");
                        sb.append("Используй 🎲 Случайное слово");
                        break;
                    }
                }
                message.setText(sb.toString());
                message.setReplyMarkup(createKeyboard());
            }
            else if (messageText.equals("/random")) {
                Dictionary.WordEntry randomWord = Dictionary.getRandomWord();
                message.setText(randomWord.formatFull());
                message.setReplyMarkup(createKeyboard());
            }
            else if (messageText.startsWith("/add")) {
                String[] parts = messageText.substring(5).split(" - ");
                if (parts.length == 2) {
                    String word = parts[0].trim();
                    String translation = parts[1].trim();

                    if (db.addWord(userId, word, translation)) {
                        message.setText("✅ *Слово добавлено!*\n\n" +
                                word + " — " + translation + "\n\n" +
                                "Продолжай пополнять словарь! 📚\n" +
                                "Посмотреть все слова: кнопка 📝 Мои слова");
                    } else {
                        message.setText("❌ Не удалось добавить слово. Попробуй еще раз.");
                    }
                } else {
                    message.setText("❌ *Неправильный формат!*\n\n" +
                            "Используй: `/add слово - перевод`\n" +
                            "Пример: `/add cat - кошка`\n\n" +
                            "Или нажми кнопку ➕ Добавить слово");
                }
                message.setReplyMarkup(createKeyboard());
            }
            else if (messageText.equals("/train")) {
                Map<String, String> word = db.getRandomWord(userId);
                if (word != null) {
                    String englishWord = word.get("word");
                    trainingSessions.put(userId, englishWord);
                    message.setText("📖 *Как переводится слово:*\n\n" +
                            "🔤 *" + englishWord + "*\n\n" +
                            "Напиши перевод 👇");
                } else {
                    message.setText("📭 *У тебя пока нет слов для тренировки!*\n\n" +
                            "Вот встроенные слова: кнопка 📖 Словарь\n" +
                            "Добавь свое слово: кнопка ➕ Добавить слово");
                    message.setReplyMarkup(createKeyboard());
                }
            }
            // Для старых пользователей: если прислали любое сообщение, показываем клавиатуру
            else if (!isNewUser && !messageText.equals("/start") && !trainingSessions.containsKey(userId)) {
                message.setText("👇 *Нажми на кнопку*");
                message.setReplyMarkup(createKeyboard());
            }

            try {
                execute(message);
            } catch (TelegramApiException e) {
                System.out.println("❌ Ошибка отправки: " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new Main());
            System.out.println("🤖 Бот успешно запущен!");
            System.out.println("📱 Найди своего бота в Telegram и нажми /start");
            System.out.println("\n📝 Кнопки: 🌟 Слово дня | 🎲 Случайное слово | 📖 Словарь | 📝 Мои слова | ➕ Добавить слово | 🎯 Тренировка | 📊 Статистика");
        } catch (TelegramApiException e) {
            System.out.println("❌ Ошибка при запуске: " + e.getMessage());
            e.printStackTrace();
        }
    }
}