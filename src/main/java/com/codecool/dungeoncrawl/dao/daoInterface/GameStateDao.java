package com.codecool.dungeoncrawl.dao.daoInterface;

import com.codecool.dungeoncrawl.model.GameState;

import java.util.List;

public interface GameStateDao {
    void add(GameState state);
    void update(GameState state);
    GameState get(int id);
    List<GameState> getAll();
    GameState getByPlayerSave(int playerId, String saveName);
}
