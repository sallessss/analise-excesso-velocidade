import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { RespostaAnalise } from '../models/analise.model';

@Injectable({ providedIn: 'root' })
export class ServicoAnalise {
  private readonly clienteHttp = inject(HttpClient);
  private readonly caminhoAnalises = '/api/analises';

  analisar(arquivo: File): Observable<RespostaAnalise> {
    const dadosFormulario = new FormData();
    dadosFormulario.append('arquivo', arquivo);

    return this.clienteHttp.post<RespostaAnalise>(this.caminhoAnalises, dadosFormulario);
  }
}
