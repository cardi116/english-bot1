package com.englishbot;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import java.util.*;

public class KeyboardFactory {

    // Кнопки для Викторины (4 варианта)
    public static InlineKeyboardMarkup createQuizKeyboard(String correct, List<String> wrongs) {
        List<String> options = new ArrayList<>(wrongs);
        options.add(correct);
        Collections.shuffle(options); // Перемешиваем, чтобы правильный ответ не был всегда первым

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for (String option : options) {
            InlineKeyboardButton btn = new InlineKeyboardButton();
            btn.setText(option);
            // Если ответ верный, шлем спец-дату, если нет — другую
            btn.setCallbackData(option.equals(correct) ? "quiz_correct" : "quiz_wrong");

            rows.add(Collections.singletonList(btn));
        }

        markup.setKeyboard(rows);
        return markup;
    }
}