package br.com.estacionamento.application.port;

import br.com.estacionamento.domain.model.PlateDetection;
import br.com.estacionamento.domain.model.PlateRead;

/** Port implemented by any camera/OCR adapter that can emit a plate observation. */
public interface PlateReader {
    PlateDetection receive(PlateRead read);
}
