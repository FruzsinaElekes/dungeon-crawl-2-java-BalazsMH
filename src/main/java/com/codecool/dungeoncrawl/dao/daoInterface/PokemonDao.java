package com.codecool.dungeoncrawl.dao.daoInterface;

import com.codecool.dungeoncrawl.model.PokemonModel;

import java.util.List;

public interface PokemonDao {
    void add(PokemonModel pokemon, int playerId);
    void update(PokemonModel pokemon, int playerId);
    PokemonModel get(int id);
    List<PokemonModel> getAll();
    List<PokemonModel> getPokemonModelsForPlayer(int playerId);
}
