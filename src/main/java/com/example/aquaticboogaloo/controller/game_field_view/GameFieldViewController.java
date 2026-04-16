package com.example.aquaticboogaloo.controller.game_field_view;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("test-view/game-field")
@RequiredArgsConstructor
public class GameFieldViewController {
    private final ObjectMapper objectMapper;

//    @GetMapping
//    public String battlefield(Model model) throws JsonProcessingException {
//        GameRuleset ruleset = new GameRuleset();
//        Game game = new Game();
//        game.setRuleset(ruleset);
//
//        GameFieldGenerator generator = new GameFieldGenerator(game);
//
//        GameFieldView view = generator.initializeShips();
//
//        model.addAttribute("battlefieldJson", objectMapper.writeValueAsString(view));
//        return "battlefield";
//    }
}
