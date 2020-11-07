package com.codecool.dungeoncrawl.logic.actors;

import com.codecool.dungeoncrawl.logic.Cell;

import java.util.List;

public class Skeleton extends Actor {

    public Skeleton(Cell cell) {
        super(cell);
    }

    @Override
    public String getTileName() {
        return "skeleton";
    }

    @Override
    public Cell getCell() {
        return super.getCell();
    }

    public void findPlayer(List<Cell> cell) {
        System.out.println(cell);
    }
}
