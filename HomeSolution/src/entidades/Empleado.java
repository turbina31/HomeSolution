package entidades;

public abstract class Empleado implements IEmpleado{
	private static int contador = 1;
    protected final Integer legajo;
    protected final String nombre;
    protected int retrasos = 0;
    protected boolean asignado;

    public Empleado(String nombre) {
    	this.legajo = contador++;
        this.nombre = nombre;
        this.asignado = false;
    }

    @Override public Integer obtenerLegajo() { return legajo; }
    @Override public String obtenerNombre() { return nombre; }
    @Override public int obtenerRetrasos() { return retrasos; }
    @Override public void sumarRetraso() { this.retrasos++; }
    @Override public void liberar() { this.asignado = false; }
    @Override public boolean estaLibre() { return !asignado; }
    @Override public boolean esDePlanta() { return false; }
    @Override
    public void marcarAsignado() {
    	this.asignado = true;
    }
    @Override
    public void marcarOcupado() {
    	this.asignado = true;
    }

    public void asignar() { this.asignado = true; }
}
