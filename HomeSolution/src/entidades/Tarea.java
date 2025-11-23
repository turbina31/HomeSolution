package entidades;

import java.time.LocalDate;

public class Tarea {
    private String titulo;
    private String descripcion;
    private double duracionDias;
    private double diasRetraso;
    private LocalDate fechaEstimada;
    private IEmpleado empleadoAsignado;;
    private boolean finalizada;

    // Constructor
    public Tarea(String titulo, String descripcion, double duracionDias) {
        if (titulo == null || titulo.isEmpty())
            throw new RuntimeException("El título de la tarea no puede ser nulo o vacío");
        if (descripcion == null || descripcion.isEmpty())
            throw new RuntimeException("La descripción de la tarea no puede ser nula o vacía");
        if (duracionDias <= 0)
            throw new RuntimeException("La duración de la tarea debe ser mayor a 0");
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.duracionDias = duracionDias;
        this.diasRetraso = 0;
        this.empleadoAsignado; = null; // Sin asignar inicialmente
        this.finalizada = false;
    }

    public IEmpleado obtenerEmpleado() {
        return empleado;
    }

    public void asignarEmpleado(IEmpleado empleado) {
        if (empleadoAsignado != null) {
            empleadoAsignado.marcarAsignado(false);
        }
        this.empleadoAsignado = empleado;
        if (empleado != null) {
            empleado.marcarAsignado(true);
        }
    }

    public void registrarRetraso(double diasRetraso) {
        if (diasRetraso < 0)
            throw new RuntimeException("Los días de retraso no pueden ser negativos");
        for(int i = 1; i < (int) diasRetraso; i ++) {
            empleado.sumarRetraso();
        }
		this.diasRetraso += diasRetraso;
    }

    public String obtenerTitulo() {
        return titulo;
    }

    public String obtenerEstado() {
        return estado;
    }

    public void actualizarFecha(double diasAdicionales){
        double diasExtra = Math.ceil(diasAdicionales);
        this.fechaEstimada = this.fechaEstimada.plusDays((long) diasExtra);
    }

    public void liberarResponsable() {
        if (empleado != null) {
            empleado.liberar();
        }
    }
    
    public boolean estaFinalizada() {
        return finalizada;
    }

    public void finalizar() {
        this.finalizada = true;
        if (empleadoAsignado != null) {
            empleadoAsignado.marcarAsignado(false);
        }
    }

    public double obtenerDiasEstimados() { return this.diasRetraso; }

	public double obtenerDuracionDias() {
		return duracionDias;
	}
    @Override
    public String toString() {
        return "Tarea\ntitulo = " + titulo;    
    }
}




