package com.tfg.tfg.service.interfaces;

public interface IDataDragonService {

    void updateChampionDatabase();

    String getChampionNameById(Long championId);

    String getChampionIconUrl(Long championId);

    String getProfileIconUrl(Integer profileIconId);
}
