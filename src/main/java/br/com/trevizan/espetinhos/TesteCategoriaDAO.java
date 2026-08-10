package br.com.trevizan.espetinhos;

import br.com.trevizan.espetinhos.dao.CategoriaDAO;
import br.com.trevizan.espetinhos.model.Categoria;

public class TesteCategoriaDAO {

    public static void main(String[] args) {

        CategoriaDAO categoriaDAO = new CategoriaDAO();

        for (Categoria categoria : categoriaDAO.listarAtivas()) {

            System.out.println(
                    categoria.getIdCategoria()
                            + " - "
                            + categoria.getNome()
            );
        }
    }
}