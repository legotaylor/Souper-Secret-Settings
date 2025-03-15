package com.nettakrim.souper_secret_settings.gui;

import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class SuggestionTextFieldWidget extends DraggableTextFieldWidget {
    protected Supplier<List<String>> validAdditions;
    protected Consumer<String> onSubmit;

    protected Consumer<String> onChange;

    private final List<String> currentSuggestions = new ArrayList<>();
    private int currentSuggestionIndex = 0;

    private final boolean resetOnEmpty;

    public boolean submitOnLostFocus = false;

    public SuggestionTextFieldWidget(int x, int width, int height, Text message, boolean resetOnEmpty) {
        super(x, width, height, message);
        super.setChangedListener(this::onChange);
        this.resetOnEmpty = resetOnEmpty;
    }

    @Override
    public void setChangedListener(Consumer<String> changedListener) {
        onChange = changedListener;
    }

    public void setListeners(Supplier<List<String>> validAdditions, Consumer<String> onSubmit) {
        this.validAdditions = validAdditions;
        this.onSubmit = onSubmit;
    }

    protected void onChange(String s) {
        if (onChange != null) onChange.accept(s);

        if (validAdditions == null) {
            setSuggestion(null);
            return;
        }

        String previousSuggestion = null;
        if (!currentSuggestions.isEmpty()) {
            previousSuggestion = currentSuggestions.get(currentSuggestionIndex);
        }

        currentSuggestions.clear();
        List<String> suggestions = validAdditions.get();

        int length = s.length();
        if (length == 0 && resetOnEmpty) {
            previousSuggestion = null;
        }

        int maxSuggestionLength = Math.max(length, 32);
        for (String suggestion : suggestions) {
            int suggestionLength = suggestion.length();
            if (suggestionLength >= length && s.equals(suggestion.substring(0, length))) {
                currentSuggestions.add(suggestion);
            }

            if (suggestionLength > maxSuggestionLength) {
                maxSuggestionLength = suggestionLength;
            }
        }
        setMaxLength(maxSuggestionLength);

        if (currentSuggestions.isEmpty()) {
            setSuggestion(null);
        } else {
            if (previousSuggestion == null) {
                currentSuggestionIndex = 0;
            } else {
                currentSuggestionIndex = currentSuggestions.indexOf(previousSuggestion);
                if (currentSuggestionIndex == -1) currentSuggestionIndex = 0;
            }
            setSuggestion(currentSuggestions.get(currentSuggestionIndex).substring(length));
        }
    }

    protected void cycleSuggestion(int direction) {
        int suggestions = currentSuggestions.size();
        if (suggestions == 0) return;

        currentSuggestionIndex += direction;
        if (currentSuggestionIndex < 0) {
            currentSuggestionIndex += suggestions;
        } else if (currentSuggestionIndex >= suggestions) {
            currentSuggestionIndex -= suggestions;
        }

        setSuggestion(currentSuggestions.get(currentSuggestionIndex).substring(getText().length()));
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.isFocused()) {
            if (keyCode == 257 && onSubmit != null) {
                onSubmit.accept(getText());
                return true;
            }
            if (!currentSuggestions.isEmpty()) {
                if (keyCode == 258) {
                    String text = getText();
                    setText(text + currentSuggestions.get(currentSuggestionIndex).substring(text.length()));
                    setCursorToEnd(false);
                    return true;
                } else if (keyCode == 265) {
                    cycleSuggestion(-1);
                    return true;
                } else if (keyCode == 264) {
                    cycleSuggestion(1);
                    return true;
                }
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void setFocused(boolean focused) {
        super.setFocused(focused);
        if (!focused && submitOnLostFocus) {
            onSubmit.accept(getText());
        }
    }
}
