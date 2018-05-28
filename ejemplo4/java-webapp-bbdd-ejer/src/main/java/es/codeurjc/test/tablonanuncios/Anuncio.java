package es.codeurjc.test.tablonanuncios;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Anuncio {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	private String nombre;
	private String asunto;
	private String comentario;

	public Anuncio() {}

	public Anuncio(String nombre, String asunto, String comentario) {
		super();
		this.nombre = nombre;
		this.asunto = asunto;
		this.comentario = comentario;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getAsunto() {
		return asunto;
	}

	public void setAsunto(String asunto) {
		this.asunto = asunto;
	}

	public String getComentario() {
		return comentario;
	}

	public void setComentario(String comentario) {
		this.comentario = comentario;
	}

	@Override
	public String toString() {
		return "Anuncio [nombre=" + nombre + ", asunto=" + asunto + ", comentario=" + comentario + "]";
	}

}
