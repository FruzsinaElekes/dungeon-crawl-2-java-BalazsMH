package com.codecool.dungeoncrawl.dao;

import com.codecool.dungeoncrawl.logic.actors.Player;
import com.codecool.dungeoncrawl.logic.actors.pokemon.Pokemon;
import com.codecool.dungeoncrawl.logic.items.Inventory;
import com.codecool.dungeoncrawl.logic.items.LootBox;
import com.codecool.dungeoncrawl.model.*;
import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GameDatabaseManager {
    private PlayerDao playerDao;
    private GameStateDao gameStateDao;
    private PokemonDao pokemonDao;
    private InventoryDao inventoryDao;
    private LootBoxDao lootBoxDao;

    private PlayerModel playerModel;
    private GameState gameStateModel;
    private InventoryModel inventoryModel;
    private List<LootBoxModel> lootBoxModels = new ArrayList<>();
    private List<PokemonModel> pokemonModels = new ArrayList<>();

    public void setup() throws SQLException {
        DataSource dataSource = connect();
        playerDao = new PlayerDaoJdbc(dataSource);
        pokemonDao = new PokemonDaoJdbc(dataSource);
        inventoryDao = new InventoryDaoJdbc(dataSource);
        gameStateDao = new GameStateDaoJdbc(dataSource, playerDao, inventoryDao);
        lootBoxDao = new LootBoxDaoJdbc(dataSource);
    }

    public void saveGameState(String map, String storedMap, Date date, String saveName){
        gameStateModel = new GameState(map, storedMap, date, playerModel, saveName);
        gameStateDao.add(gameStateModel);
    }

    public void updateGameState(String map, String storedMap, Date date){
        gameStateModel.setCurrentMap(map);
        gameStateModel.setStoredMap(storedMap);
        gameStateModel.setSavedAt(date);
        gameStateDao.update(gameStateModel);
    }

    public void savePlayer(Player player) {
        playerModel = new PlayerModel(player);
        playerDao.add(playerModel);
    }

    public void updatePlayer(Player player){
        playerModel.setX(player.getX());
        playerModel.setY(player.getY());
        playerModel.setLevel(player.getLevel());
        playerDao.update(playerModel);
    }

    public void savePokemon(Pokemon pokemon){
        PokemonModel model = new PokemonModel(pokemon);
        pokemonDao.add(model, playerModel.getId());
    }

    public void updatePokemon(Pokemon pokemon){
        PokemonModel model = new PokemonModel(pokemon);
        pokemonDao.update(model, playerModel.getId());
    }

    public void saveInventory(Inventory inventory){
        inventoryModel = new InventoryModel(inventory);
        inventoryModel.setPlayerId(playerModel.getId());
        inventoryDao.add(inventoryModel);
    }

    public void updateInventory(Inventory inventory) {
        inventoryModel.setHealthPotionNumber(inventory.getHealthPotionNumber());
        inventoryModel.setPokeBallNumber(inventory.getPokeBallNumber());
        inventoryModel.setKey(inventory.hasKey());
        inventoryModel.setActivePokemonId(inventory.getActivePokemon().getPokeId());
        inventoryDao.update(inventoryModel);
    }

    public void saveLootbox(LootBox lootBox){
        LootBoxModel lootboxModel = new LootBoxModel(lootBox);
        lootboxModel.setPlayerId(playerModel.getId());
        lootBoxModels.add(lootboxModel);
        lootBoxDao.add(lootboxModel);
    }

    public void updateLootbox(LootBox lootBox){
        int idx = lootBoxModels.indexOf(lootBoxModels.stream()
                .filter(l -> l.getLootBoxId() == lootBox.getLootBoxId())
                .collect(Collectors.toList())
                .get(0));
        System.out.println("current box: " +lootBoxModels.get(idx).getLevel());
        if (lootBox.getLevel() == 0){
            System.out.println("found");
            lootBoxModels.get(idx).setLevel(0);
            lootBoxModels.get(idx).setX(null);
            lootBoxModels.get(idx).setY(null);
        }
        lootBoxDao.update(lootBoxModels.get(idx));
    }

    public PokemonModel getPokemon(int id){
        return pokemonDao.get(id);
    }

    public List<PokemonModel> getPokemonModels(int playerId) {
        return pokemonDao.getPokemonModelsForPlayer(playerId);
    }

    public List<PokemonModel> getAllPokemon(){
        return pokemonDao.getAll();
    }

    public List<GameState> getSaves(){
        return gameStateDao.getAll();
    }

    public void loadGame(String playerName, String saveName) {
        gameStateModel = gameStateDao.getByPlayerSave(playerName, saveName);
        fillUpAllModels();
    }
    public void loadGame(int gameId) {
        gameStateModel = gameStateDao.get(gameId);
        fillUpAllModels();
    }

    private void fillUpAllModels(){
        playerModel = gameStateModel.getPlayer();
        inventoryModel = gameStateModel.getInventoryModel();
        pokemonModels = pokemonDao.getPokemonModelsForPlayer(playerModel.getId());
        lootBoxModels = lootBoxDao.getAllForPlayerId(playerModel.getId());
        gameStateModel.setLootBoxModelList(lootBoxModels);
        gameStateModel.setPokemonModelList(pokemonModels);
    }

    public GameState getGameStateModel(){
        return gameStateModel;
    }


    private DataSource connect() throws SQLException {
        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        String dbName = System.getenv("dbName");
        String user = System.getenv("user");
        String password = System.getenv("password");

        dataSource.setDatabaseName(dbName);
        dataSource.setUser(user);
        dataSource.setPassword(password);

        System.out.println("Trying to connect");
        dataSource.getConnection().close();
        System.out.println("Connection ok.");

        return dataSource;
    }

}
