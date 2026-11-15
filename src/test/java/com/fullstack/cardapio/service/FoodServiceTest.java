package com.fullstack.cardapio.service;

import com.fullstack.cardapio.entity.Food;
import com.fullstack.cardapio.repository.FoodRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FoodServiceTest {

    @Mock
    private FoodRepository repository;

    @InjectMocks
    private FoodService service;

    private Food alimento(String nome, Integer preco) {
        return new Food(null, nome, "https://exemplo.com/imagem.jpg", preco);
    }

    private void afirmarRejeicao(Food food, String mensagem) {
        IllegalArgumentException erro = assertThrows(
                IllegalArgumentException.class, () -> service.cadastrar(food));
        assertEquals(mensagem, erro.getMessage());
        verifyNoInteractions(repository);
    }

    @Test
    @DisplayName("CT-01 - Consulta retorna lista vazia")
    void consultaListaVazia() {
        when(repository.findAll()).thenReturn(List.of());
        List<Food> resultado = service.listar();
        assertAll("Lista vazia", () -> assertNotNull(resultado),
                () -> assertTrue(resultado.isEmpty()));
        verify(repository, times(1)).findAll();
        verifyNoMoreInteractions(repository);
    }

    @Test
    @DisplayName("CT-02 - Consulta retorna um alimento")
    void consultaUmAlimento() {
        Food pizza = alimento("Pizza", 30);
        when(repository.findAll()).thenReturn(List.of(pizza));
        List<Food> resultado = service.listar();
        assertAll("Um alimento", () -> assertEquals(1, resultado.size()),
                () -> assertSame(pizza, resultado.get(0)),
                () -> assertEquals("Pizza", resultado.get(0).getTitle()));
        verify(repository).findAll();
    }

    @Test
    @DisplayName("CT-03 - Consulta retorna múltiplos alimentos")
    void consultaMultiplosAlimentos() {
        Food pizza = alimento("Pizza", 30);
        Food suco = alimento("Suco", 10);
        when(repository.findAll()).thenReturn(List.of(pizza, suco));
        List<Food> resultado = service.listar();
        assertAll("Dois alimentos", () -> assertEquals(2, resultado.size()),
                () -> assertSame(pizza, resultado.get(0)),
                () -> assertSame(suco, resultado.get(1)));
        verify(repository).findAll();
    }

    @Test
    @DisplayName("CT-04 - Cadastro válido encaminha dados corretos")
    void cadastroValido() {
        Food entrada = alimento("Hambúrguer", 25);
        when(repository.save(entrada)).thenReturn(entrada);
        Food resultado = service.cadastrar(entrada);
        assertAll("Cadastro válido", () -> assertNotNull(resultado),
                () -> assertEquals("Hambúrguer", resultado.getTitle()),
                () -> assertEquals(25, resultado.getPrice()));
        ArgumentCaptor<Food> captor = ArgumentCaptor.forClass(Food.class);
        verify(repository, times(1)).save(captor.capture());
        assertSame(entrada, captor.getValue());
    }

    @Test
    @DisplayName("CT-05 - Cadastro retorna identificador simulado")
    void cadastroRetornaId() {
        Food entrada = alimento("Pastel", 12);
        Food salvo = new Food(42L, "Pastel", "https://exemplo.com/imagem.jpg", 12);
        when(repository.save(entrada)).thenReturn(salvo);
        Food resultado = service.cadastrar(entrada);
        assertAll("Identificador", () -> assertNotNull(resultado),
                () -> assertEquals(42L, resultado.getId()),
                () -> assertEquals("Pastel", resultado.getTitle()));
        verify(repository).save(entrada);
    }

    @Test
    @DisplayName("CT-06 - Cadastro preserva preço e caracteres acentuados")
    void cadastroPreservaDados() {
        Food entrada = alimento("Pão de queijo", 10);
        when(repository.save(entrada)).thenReturn(entrada);
        Food resultado = service.cadastrar(entrada);
        assertAll("Dados preservados",
                () -> assertEquals("Pão de queijo", resultado.getTitle()),
                () -> assertEquals(10, resultado.getPrice()),
                () -> assertEquals("https://exemplo.com/imagem.jpg", resultado.getImage()));
        verify(repository).save(entrada);
    }

    @Test
    @DisplayName("CT-07 - Rejeita alimento nulo")
    void rejeitaAlimentoNulo() {
        afirmarRejeicao(null, "Alimento não informado");
    }

    @Test
    @DisplayName("CT-08 - Rejeita nome nulo")
    void rejeitaNomeNulo() {
        afirmarRejeicao(alimento(null, 20), "Nome obrigatório");
    }

    @Test
    @DisplayName("CT-09 - Rejeita nome vazio")
    void rejeitaNomeVazio() {
        afirmarRejeicao(alimento("", 20), "Nome obrigatório");
    }

    @Test
    @DisplayName("CT-10 - Rejeita nome composto por espaços")
    void rejeitaNomeSoEspacos() {
        afirmarRejeicao(alimento("   ", 20), "Nome obrigatório");
    }

    @Test
    @DisplayName("CT-11 - Rejeita preço nulo")
    void rejeitaPrecoNulo() {
        afirmarRejeicao(new Food(null, "Coxinha", "https://exemplo.com/imagem.jpg", null), "Preço inválido");
    }

    @Test
    @DisplayName("CT-12 - Rejeita preço negativo")
    void rejeitaPrecoNegativo() {
        afirmarRejeicao(alimento("Pizza", -1), "Preço inválido");
    }

    @Test
    @DisplayName("CT-13 - Rejeita imagem nula")
    void rejeitaImagemNula() {
        afirmarRejeicao(new Food(null, "Pizza", null, 30), "Imagem obrigatória");
    }

    @Test
    @DisplayName("CT-14 - Rejeita imagem com URL vazia")
    void rejeitaImagemVazia() {
        afirmarRejeicao(new Food(null, "Pizza", "", 30), "URL inválida");
    }

    @Test
    @DisplayName("CT-15 - Rejeita URL sem domínio")
    void rejeitaUrlSemDominio() {
        afirmarRejeicao(new Food(null, "Pizza", "https:///foto.jpg", 30), "URL inválida");
    }

    @Test
    @DisplayName("CT-16 - Rejeita protocolo não permitido")
    void rejeitaProtocoloFtp() {
        afirmarRejeicao(new Food(null, "Pizza", "ftp://exemplo.com/foto.jpg", 30), "URL inválida");
    }

    @Test
    @DisplayName("CT-17 - Propaga falha na consulta")
    void trataErroConsulta() {
        when(repository.findAll()).thenThrow(new RuntimeException("Erro de conexão"));
        RuntimeException erro = assertThrows(RuntimeException.class, () -> service.listar());
        assertEquals("Erro de conexão", erro.getMessage());
        verify(repository, times(1)).findAll();
    }

    @Test
    @DisplayName("CT-18 - Propaga falha no salvamento")
    void trataErroCadastro() {
        Food entrada = alimento("Batata", 15);
        when(repository.save(entrada)).thenThrow(new RuntimeException("Falha ao salvar"));
        RuntimeException erro = assertThrows(RuntimeException.class, () -> service.cadastrar(entrada));
        assertEquals("Falha ao salvar", erro.getMessage());
        verify(repository, times(1)).save(entrada);
    }

    @Test
    @DisplayName("CT-19 - Rejeita URL com formato totalmente inválido para URI")
    void rejeitaUrlComErroDeUri() {
        Food entrada = new Food(null, "Pizza", "http://[invalid-ipv6", 30);
        afirmarRejeicao(entrada, "URL inválida");
    }

    @Test
    @DisplayName("CT-20 - Rejeita URL com esquema diferente de http ou https (ex: ftp)")
    void rejeitaEsquemaInvalido() {
        Food entrada = new Food(null, "Pizza", "ftp://site.com/img.jpg", 30);
        afirmarRejeicao(entrada, "URL inválida");
    }

    @Test
    @DisplayName("CT-21 - Rejeita URL sem host válido")
    void rejeitaUrlSemHost() {
        Food entrada = new Food(null, "Pizza", "http://", 30);
        afirmarRejeicao(entrada, "URL inválida");
    }
}