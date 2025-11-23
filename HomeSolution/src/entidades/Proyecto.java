package entidades;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

public class Proyecto {
	private int contador = 1;
	private Integer codigoProyecto;
    private String domicilio;
    private String fechaInicio;
    private String fechaFin;
    private LocalDate fechaPrincipio;
    private Map<String, Cliente> clientes;
    private List<Tarea> tareas;
    private Map<String, Tarea> tarea;
    private String estado; // Reemplaza finalizado
    private Estado tipoDeEstado = new Estado();
    private Set<IEmpleado> historialEmpleados;
    private boolean tieneRetraso;

    public Proyecto(int codigoProyecto, String domicilio, String fechaInicio, String fechaFin) {
        if (codigoProyecto < 0)
            throw new RuntimeException("El código del proyecto debe ser mayor o igual a 0");
        if (fechaInicio == null)
            throw new RuntimeException("La fecha de inicio no puede ser nula");
        // Convertir fechas
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate inicio, fin;
        try {
            inicio = LocalDate.parse(fechaInicio, formatter);
            fin = LocalDate.parse(fechaFin, formatter);
        } catch (DateTimeParseException e) {
            throw new RuntimeException("Formato de fecha inválido, debe ser yyyy-mm-dd");
        }
        if (fin.isBefore(inicio))
            throw new RuntimeException("La fecha de fin no puede ser anterior a la fecha de inicio");

        this.codigoProyecto = this.contador++;
        this.domicilio = domicilio;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.fechaPrincipio = inicio;
        this.clientes = new HashMap<String, Cliente>();
        this.tarea = new HashMap<>();
        this.tareas = new ArrayList<>();
        this.empleadosLibres = new Stack<>();
		this.tieneRetraso = false;
        this.estado = tipoDeEstado.pendiente;
        this.historialEmpleados = new HashSet<>();
    }

    public void agregarCliente(Cliente c) {
    	clientes.put(c.obtenerEmail(), c);
    }
    
    public void agregarTarea(String titulo, String descripcion, double dias) {
        if (finalizado) 
			throw new IllegalStateException("No se pueden agregar tareas a proyecto finalizado");
        tareas.add(new Tarea(titulo, descripcion, dias));
    }

    public void asignarEmpleado(Tarea tarea, IEmpleado empleado) {
		try {
            // Validar que el proyecto no esté finalizado
            if (this.estado.equals(Estado.finalizado))
                throw new RuntimeException("No se pueden asignar empleados a un proyecto finalizado");

            // Validar que el empleado esté libre (si no es null)
            if (empleado != null && !empleado.estaLibre())
                throw new RuntimeException("El empleado debe estar libre");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        // Verificar que la tarea pertenece al proyecto
        boolean tareaEncontrada = false;
        Iterator<Tarea> este = tareas.iterator();
        while (este.hasNext()) {
            if (este.next().obtenerTitulo().equals(tarea.obtenerTitulo())) { // Comparación por referencia
                tareaEncontrada = true;
            }
        }
        try {
            if (!tareaEncontrada)
                throw new RuntimeException("La tarea no pertenece al proyecto");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        // Liberar empleado anterior, si existe
        IEmpleado empleadoAnterior = tarea.obtenerEmpleado();
        if (empleadoAnterior != null && !empleadoAnterior.estaLibre()) {
            empleadoAnterior.estaLibre();
        }

        // Asignar nuevo empleado
        tarea.asignarEmpleado(empleado);

        // Actualizar historial si el empleado no es null
        if (empleado != null) {
            historialEmpleados.add(empleado);
        }
    }

    public String obtenerEstado() {
        if (finalizado) {
            return Estado.finalizado;
        }

        // Verificar si tiene al menos una tarea con empleado asignado
        for (Tarea t : tareas) {
            if (t.empleadoAsignado() != null) {
                return Estado.activo;
            }
        }

        return Estado.pendiente;
    }

	public Tarea obtenerTarea(String titulo) {
        if(!perteneceAestaTarea(titulo)){
            throw new RuntimeException("La tarea no pertenece al proyecto");
        }
        Tarea tareaEncontrada = null;

        for(Tarea tarea : tareas) {
            if(tarea.obtenerTitulo().equals(titulo)) {
                tareaEncontrada = tarea;
            }
        }

        return tareaEncontrada;
    }
	
    public List<Tarea> obtenerTareas() {
        return new ArrayList<>(tareas); // Copia defensiva
    }

    public boolean perteneceAestaTarea(String titulo){
        Iterator<Tarea> it = obtenerTareas().iterator();
        while (it.hasNext()) {
            if (it.next().obtenerTitulo().equals(titulo)) {
                return true;
            }
        }
        throw new RuntimeException("La tarea no pertenece al proyecto");
    }

    public void liberarEmpleadoAsignado(String titulo){
        Tarea t = tarea.get(titulo);
        IEmpleado empleado = t.obtenerEmpleado();
        if (empleado != null && !empleado.estaLibre()) {
            empleado.estaLibre();
            t.asignarEmpleado(null);
        }
    }

    public boolean verificarTodasTareasFinalizadas() {
        Iterator<Tarea> it = obtenerTareas().iterator();

        while (it.hasNext()) {
            if (!it.next().estaFinalizada()) {
                return false;
            }
        }
        return true;
    }

    public void finalizarProyecto(String fechaFin) {
        LocalDate fechaReal = LocalDate.parse(fechaFin);

    // REGLA DEL ENUNCIADO: no se puede finalizar antes del inicio
    if (fechaReal.isBefore(this.inicio)) {
        throw new IllegalArgumentException("La fecha de finalización no puede ser anterior a la de inicio");
    }

    // REGLA DEL TEST: espera excepción si se finaliza antes del fin previsto
    if (fechaReal.isBefore(this.finPrevisto)) {
        throw new IllegalArgumentException("No se puede finalizar antes de la fecha prevista");
    }

    this.finReal = fechaReal;
    this.finalizado = true;

    // LIBERAR A TODOS LOS EMPLEADOS DEL PROYECTO
    for (Tarea t : tareas) {
        IEmpleado e = t.empleadoAsignado();
        if (e != null) {
            e.marcarAsignado(false);
        }
    }
    }
    
    public double calcularCostoTotal(HashMap<String, IEmpleado> empleados) {
        double costo = 0;
        for (Tarea t : tareas) {
            if (t.obtenerEmpleado() != null) {
                costo += t.obtenerEmpleado().calcularCosto(t.obtenerDiasEstimados());
            }
        }
        // Adicional 2% para empleados de planta si NO hay retraso
        if (!tieneRetraso) {
            for (Tarea t : tareas) {
                IEmpleado e = t.obtenerEmpleado();
                if (e != null && e.esDePlanta()) {
                    costo += 0.02 * e.calcularCosto(t.obtenerDiasEstimados());
                }
            }
        }
        return costo;
    }
   
	public Integer consultarCodigoProyecto(int codigoProyecto) {
		return this.codigoProyecto = codigoProyecto;
	}

	public Integer obtenerNumero() {
		return codigoProyecto;
	}

	public String obtenerDomicilio() {
		return domicilio;
	}

	public LocalDate obtenerFechaInicio() {
        return LocalDate.parse(fechaInicio, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }
	
    public Set<IEmpleado> ObtenerHistorialDeEmpleados() {
        return historialEmpleados;
    }
	@Override
    public String toString() {
        return "Proyecto \ncodigoProyecto = " + codigoProyecto + ",\n domicilio = " + domicilio + 
        ",\n fechaInicio = " + fechaInicio + ",\n fechaFin = " + fechaFin + ",\n clientes = " + clientes + 
        ",\n tareas = " + tareas + ",\n estado = " + estado;
    }
    public String mostrarInfo() {
        return "Proyecto: " +
            "codigo=" + codigoProyecto +
            ", domicilio='" + domicilio + '\'' +
            ", inicio='" + fechaInicio + '\'' +
            ", fin='" + fechaFin + '\'' +
            ", estado='" + estado + '\'' +
            ", tareas=" + (tareas != null ? tareas.size() : 0);
    }
}





