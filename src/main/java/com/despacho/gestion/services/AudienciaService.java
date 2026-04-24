package com.despacho.gestion.services;

import com.despacho.gestion.dto.AudienciaDTO;
import com.despacho.gestion.dto.AudienciaMapper;
import com.despacho.gestion.dto.AudienciaRequestDTO;
import com.despacho.gestion.models.*;
import com.despacho.gestion.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AudienciaService {

    private final AudienciaRepository audienciaRepository;
    private final AudienciaAbogadoRepository audienciaAbogadoRepository;
    private final ExpedienteRepository expedienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final CatTribunalRepository tribunalRepository;
    private final CatTipoAudienciaRepository tipoAudienciaRepository;

    // ── Consultas ─────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<AudienciaDTO> findAll(String estado, Long abogadoId, LocalDate fechaDesde,
            LocalDate fechaHasta) {
        EstadoAudiencia e = null;
        if (estado != null && !estado.isBlank()) {
            try {
                e = EstadoAudiencia.valueOf(estado.toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }
        return AudienciaMapper.toDTOList(
            audienciaRepository.findFiltrosCombinados(e, abogadoId, fechaDesde, fechaHasta)
        );
    }

    @Transactional(readOnly = true)
    public Optional<AudienciaDTO> findById(Long id) {
        return audienciaRepository.findByIdConDetalle(id).map(AudienciaMapper::toDTO);
    }

    // ── Crear ─────────────────────────────────────────────────────────

    @Transactional
    public AudienciaDTO crear(AudienciaRequestDTO req) {
        Audiencia audiencia = new Audiencia();
        poblarDesdeRequest(audiencia, req);
        audiencia.setEstado(EstadoAudiencia.PROGRAMADA);
        audiencia.setCreatedBy(usuarioActual());

        // Guardamos la audiencia primero para obtener el ID
        Audiencia guardada = audienciaRepository.save(audiencia);

        // Asignamos abogados usando la relación
        asignarAbogados(guardada, req.getAbogadoIds(), req.getAbogadoTitularId());

        return AudienciaMapper.toDTO(guardada);
    }

    // ── Editar ────────────────────────────────────────────────────────

    @Transactional
    public AudienciaDTO editar(Long id, AudienciaRequestDTO req) {
        Audiencia audiencia = audienciaRepository.findByIdConDetalle(id)
                .orElseThrow(() -> new RuntimeException("Audiencia no encontrada: " + id));

        // No se puede editar una audiencia FINALIZADA
        if (audiencia.getEstado() == EstadoAudiencia.REALIZADA) {
            throw new RuntimeException("No se puede editar una audiencia ya realizada.");
        }

        poblarDesdeRequest(audiencia, req);
        Audiencia guardada = audienciaRepository.save(audiencia);

        // Reasignar abogados
        if (req.getAbogadoIds() != null) {
            audienciaAbogadoRepository.deleteByIdAudienciaId(id);
            asignarAbogados(guardada, req.getAbogadoIds(), req.getAbogadoTitularId());
        }
        return AudienciaMapper.toDTO(guardada);
    }

    // ── Cambiar estado ────────────────────────────────────────────────

    @Transactional
    public AudienciaDTO cambiarEstado(Long id, EstadoAudiencia nuevoEstado) {
        Audiencia audiencia = audienciaRepository.findByIdConDetalle(id)
                .orElseThrow(() -> new RuntimeException("Audiencia no encontrada: " + id));
        audiencia.setEstado(nuevoEstado);
        Audiencia guardada = audienciaRepository.save(audiencia);
        return AudienciaMapper.toDTO(guardada);
    }

    // ── Registrar resultado (abogado) ─────────────────────────────────

    @Transactional
    public AudienciaDTO registrarResultado(Long id, String resultado) {
        Audiencia audiencia = audienciaRepository.findByIdConDetalle(id)
                .orElseThrow(() -> new RuntimeException("Audiencia no encontrada: " + id));
        audiencia.setResultado(resultado);
        audiencia.setEstado(EstadoAudiencia.REALIZADA);
        Audiencia guardada = audienciaRepository.save(audiencia);
        return AudienciaMapper.toDTO(guardada);
    }

    // ── Helpers ───────────────────────────────────────────────────────

    private void poblarDesdeRequest(Audiencia audiencia, AudienciaRequestDTO req) {
        // Expediente
        Expediente expediente = expedienteRepository.findById(req.getExpedienteId()).orElseThrow(
                () -> new RuntimeException("Expediente no encontrado: " + req.getExpedienteId()));
        if (expediente.getEstado() == EstadoExpediente.FINALIZADO) {
            throw new RuntimeException(
                    "No se pueden agregar audiencias a un expediente FINALIZADO.");
        }
        audiencia.setExpediente(expediente);

        // Tipo de audiencia
        CatTipoAudiencia tipo = tipoAudienciaRepository.findById(req.getTipoAudienciaId())
                .orElseThrow(() -> new RuntimeException("Tipo de audiencia no encontrado"));
        audiencia.setTipoAudiencia(tipo);

        // Tribunal
        CatTribunal tribunal = tribunalRepository.findById(req.getTribunalId())
                .orElseThrow(() -> new RuntimeException("Tribunal no encontrado"));
        audiencia.setTribunal(tribunal);

        // Fecha y hora
        if (req.getFecha() != null) {
            audiencia.setFecha(LocalDate.parse(req.getFecha()));
        }
        if (req.getHora() != null && !req.getHora().isBlank()) {
            audiencia.setHora(LocalTime.parse(req.getHora()));
        }
    }

    private void asignarAbogados(Audiencia audiencia, List<Long> abogadoIds, Long titularId) {
        if (abogadoIds == null || abogadoIds.isEmpty()) {
            return;
        }

        for (Long abogadoId : abogadoIds) {
            Usuario abogado = usuarioRepository.findById(abogadoId)
                    .orElseThrow(() -> new RuntimeException("Abogado no encontrado: " + abogadoId));

            AudienciaAbogado aa = new AudienciaAbogado();
            AudienciaAbogadoId aaId = new AudienciaAbogadoId();
            aaId.setAudienciaId(audiencia.getId());
            aaId.setUsuarioId(abogadoId);
            aa.setId(aaId);
            aa.setAudiencia(audiencia);
            aa.setUsuario(abogado);
            aa.setEsTitular(abogadoId.equals(titularId));

            audienciaAbogadoRepository.save(aa);
        }
    }

    private Usuario usuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null)
            throw new RuntimeException("No hay sesión activa");
        return usuarioRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }
}
