package com.tfg.tfg.service.storage;

public interface IDataDragonService {

    void updateChampionDatabase();

    String getChampionNameById(Long championId);

    String getChampionIconUrl(Long championId);

    String getProfileIconUrl(Integer profileIconId);
}
