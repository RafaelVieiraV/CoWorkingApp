package ec.edu.espe.coworkingapp.pubsub;

import ec.edu.espe.coworkingapp.dto.response.BookingResponseDto;

import java.util.List;

/**
 Se arma a partir de BookingResponseDto
 */
public record BookingPipelineResult(
        int totalReservasEvaluadas,
        List<BookingResponseDto> reservasProcesadas,
        boolean huboReservaAnomala,
        String detalleAnomalia
) {
}