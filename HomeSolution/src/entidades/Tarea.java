package entidades;

import java.time.LocalDate;

public class Tarea {
    private String titulo;
    private String descripcion;
    private double duracionDias;
    private double diasRetraso;
    private LocalDate fechaEstimada;
    private IEmpleado empleado;
    private EnpleadoDePlanta enpleadoDePlanta;
    private boolean finalizada; // Nuevo campo
    private String estado; // Reemplaza finalizado
    private final Estado tipoDeEstado = new Estado();

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
        this.empleado = null; // Sin asignar inicialmente
        this.enpleadoDePlanta = null;
        this.finalizada = false;
        this.estado = tipoDeEstado.pendiente;
    }

    public IEmpleado obtenerEmpleado() {
        return empleado;
    }

    public EnpleadoDePlanta obtenerEmpleadoDePlanta(){
        return enpleadoDePlanta;
    }

    public void asignarEmpleado(IEmpleado empleado) {
        if (empleado == null)
            throw new RuntimeException("El empleado no puede ser nulo");
        if (!empleado.estaLibre())
            throw new RuntimeException("El empleado no está libre");
        if (this.empleado != null)
            this.empleado.estaLibre(); // Liberar empleado anterior, si existe
        this.empleado = empleado;
        empleado.marcarAsignado();
    }

    public void registrarRetraso(double diasRetraso) {
        if (diasRetraso < 0)
            throw new RuntimeException("Los días de retraso no pueden ser negativos");
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

    public void marcarComoFinalizada() {
        if (finalizada)
            throw new RuntimeException("La tarea ya está finalizada");
        this.finalizada = true;
    }

    public double obtenerDiasEstimados() { return this.diasRetraso; }

	public double obtenerDuracionDias() {
		return duracionDias;
	}
    
}
