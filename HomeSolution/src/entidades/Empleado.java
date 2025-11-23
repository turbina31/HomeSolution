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
    @Override
	public boolean estaAsignado() {
		return asignado;
	}

	@Override
	public abstract double calcularCosto(double dias);

	 @Override
	 public void marcarAsignado(boolean asignado) {
		this.asignado = asignado;
	 }
	
	@Override
    public String toString() {
        return getClass().getSimpleName() + "Empleado\nlegajo = " + legajo + ",\n nombre = " + 
        nombre + ",\n retrasos = " + retrasos + ",\n asignado = " + asignado;
    }
}



