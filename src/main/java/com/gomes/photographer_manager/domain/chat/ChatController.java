package com.gomes.photographer_manager.domain.chat;

import com.gomes.photographer_manager.domain.usuario.User;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/conversations")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public ResponseEntity<ConversationResponse> startConversation(@AuthenticationPrincipal User user,
                                                                  @Valid @RequestBody StartConversationRequest request) {
        ConversationResponse response = chatService.startConversation(
                user.getId(), request.otherUserId(), request.eventId());
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ConversationResponse>> findMyConversations(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(chatService.findMyConversations(user.getId()));
    }

    @GetMapping("/{id}/messages")
    public ResponseEntity<List<MessageResponse>> findMessages(@AuthenticationPrincipal User user,
                                                              @PathVariable String id,
                                                              @RequestParam(defaultValue = "0") int page,
                                                              @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(chatService.findMessages(id, page, size, user.getId()));
    }

    @PostMapping("/{id}/messages")
    public ResponseEntity<MessageResponse> sendMessage(@AuthenticationPrincipal User user,
                                                       @PathVariable String id,
                                                       @Valid @RequestBody SendMessageRequest request) {
        MessageResponse response = chatService.sendMessage(id, request.content(), user.getId());
        return ResponseEntity.status(201).body(response);
    }
}
