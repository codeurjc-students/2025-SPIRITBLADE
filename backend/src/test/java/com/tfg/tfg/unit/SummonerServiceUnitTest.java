package com.tfg.tfg.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.tfg.tfg.model.entity.Summoner;
import com.tfg.tfg.repository.SummonerRepository;
import com.tfg.tfg.service.SummonerService;

@ExtendWith(MockitoExtension.class)
class SummonerServiceUnitTest {

    @Mock
    private SummonerRepository summonerRepository;

    private SummonerService summonerService;

    @BeforeEach
    void setUp() {
        summonerService = new SummonerService(summonerRepository);
    }

    @Test
    void testFindAll() {
        Pageable pageable = mock(Pageable.class);
        Page<Summoner> page = mock(Page.class);
        when(summonerRepository.findAll(pageable)).thenReturn(page);

        Page<Summoner> result = summonerService.findAll(pageable);

        assertEquals(page, result);
        verify(summonerRepository).findAll(pageable);
    }

    @Test
    void testFindById() {
        Long id = 1L;
        Summoner summoner = new Summoner();
        when(summonerRepository.findById(id)).thenReturn(Optional.of(summoner));

        Optional<Summoner> result = summonerService.findById(id);

        assertTrue(result.isPresent());
        assertEquals(summoner, result.get());
        verify(summonerRepository).findById(id);
    }

    @Test
    void testFindByName() {
        String name = "TestPlayer";
        Summoner summoner = new Summoner();
        when(summonerRepository.findByName(name)).thenReturn(Optional.of(summoner));

        Optional<Summoner> result = summonerService.findByName(name);

        assertTrue(result.isPresent());
        assertEquals(summoner, result.get());
        verify(summonerRepository).findByName(name);
    }

    @Test
    void testFindRecentSearches() {
        List<Summoner> list = List.of(new Summoner());
        when(summonerRepository.findTop10ByOrderByLastSearchedAtDesc()).thenReturn(list);

        List<Summoner> result = summonerService.findRecentSearches();

        assertEquals(list, result);
        verify(summonerRepository).findTop10ByOrderByLastSearchedAtDesc();
    }
}