package com.fullstack.cardapio.service;

import com.fullstack.cardapio.entity.Food;
import com.fullstack.cardapio.repository.FoodRepository;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.List;

@Service
public class FoodService {

    private final FoodRepository repository;

    public FoodService(FoodRepository repository) {
        this.repository = repository;
    }

    public List<Food> listar() {
        return repository.findAll();
    }

    public Food cadastrar(Food food) {
        if (food == null) {
            throw new IllegalArgumentException("Alimento não informado");
        }
        if (food.getTitle() == null || food.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Nome obrigatório");
        }
        if (food.getPrice() == null || food.getPrice() < 0) {
            throw new IllegalArgumentException("Preço inválido");
        }
        if (food.getImage() == null) {
            throw new IllegalArgumentException("Imagem obrigatória");
        }
        if (!isUrlValida(food.getImage())) {
            throw new IllegalArgumentException("URL inválida");
        }
        return repository.save(food);
    }

public static boolean isUrlValida(String urlStr) {
        if (urlStr == null || urlStr.isBlank()) {
            return false;
        }
        try {
            java.net.URI uri = java.net.URI.create(urlStr);
            String esquema = uri.getScheme();
            
            // Valida se o esquema é estritamente http ou https e se possui host válido
            boolean esquemaValido = "http".equalsIgnoreCase(esquema) || "https".equalsIgnoreCase(esquema);
            boolean temHost = uri.getHost() != null && !uri.getHost().isBlank();
            
            return esquemaValido && temHost;
        } catch (Exception e) {
            // Se cair aqui, a string não é uma URI válida (ex: caracteres proibidos, espaços, etc.)
            return false;
        }
    }
} 