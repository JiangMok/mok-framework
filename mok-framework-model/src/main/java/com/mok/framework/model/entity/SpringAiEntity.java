package com.mok.framework.model.entity;

import java.util.Objects;

/**
 *
 * @author: mok
 * @date: 2026/6/26
 */
public class SpringAiEntity {
    String id;
    String conversationId;
    String userInput;

    @Override
    public String toString() {
        return "SpringAiEntity{" +
                "id='" + id + '\'' +
                ", conversationId='" + conversationId + '\'' +
                ", userInput='" + userInput + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SpringAiEntity that = (SpringAiEntity) o;
        return Objects.equals(id, that.id) && Objects.equals(conversationId, that.conversationId) && Objects.equals(userInput, that.userInput);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, conversationId, userInput);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getUserInput() {
        return userInput;
    }

    public void setUserInput(String userInput) {
        this.userInput = userInput;
    }
}
