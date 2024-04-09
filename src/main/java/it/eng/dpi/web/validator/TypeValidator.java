/*
 * Engineering Ingegneria Informatica S.p.A.
 *
 * Copyright (C) 2023 Regione Emilia-Romagna
 * <p/>
 * This program is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Affero General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <https://www.gnu.org/licenses/>.
 */

package it.eng.dpi.web.validator;

import it.eng.spagoCore.error.EMFError;
import it.eng.spagoLite.message.MessageBox;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

public class TypeValidator {
    private MessageBox messageBox = null;

    public TypeValidator(MessageBox messageBox) {
        this.messageBox = messageBox;
    }

    public MessageBox getMessageBox() {
        return this.messageBox;
    }

    /**
     * Metodo di validazione delle date e degli orari inseriti nei filtri di ricerca
     * 
     * @param data_da
     *            data da
     * @param ore_da
     *            ore da
     * @param minuti_da
     *            minuti da
     * @param data_a
     *            data da
     * @param ore_a
     *            ore a
     * @param minuti_a
     *            minuti a
     * @param nm_data_da
     *            descrizione campo data da
     * @param nm_data_a
     *            descrizione campo data a
     * 
     * @return dateValidate, un array di Date contenente le date da - a validate
     * 
     * @throws EMFError
     *             errore generico
     */
    public Date[] validaDate(Date data_da, BigDecimal ore_da, BigDecimal minuti_da, Date data_a, BigDecimal ore_a,
            BigDecimal minuti_a, String nm_data_da, String nm_data_a) throws EMFError {
        Date[] dateValidate = new Date[2];
        dateValidate[0] = null;
        dateValidate[1] = null;
        if (data_da != null || ore_da != null || minuti_da != null || data_a != null || ore_a != null
                || minuti_a != null) {
            // Verifico che i campi data, ora e minuti siano validi
            isDateValid(data_da, ore_da, minuti_da, nm_data_da);
            isDateValid(data_a, ore_a, minuti_a, nm_data_a);

            // Controllo che i campi degli orari e minuti siano validi (23 ore
            // 59 minuti)
            isTimeValid(ore_da, minuti_da, "Ora da");
            isTimeValid(ore_a, minuti_a, "Ora a");

            // Mi serve un controllo per sapere se l'utente ha inserito la data
            // corrente
            Calendar data_odierna = Calendar.getInstance();
            data_odierna.set(Calendar.HOUR_OF_DAY, 0);
            data_odierna.set(Calendar.MINUTE, 0);
            data_odierna.set(Calendar.SECOND, 0);
            data_odierna.set(Calendar.MILLISECOND, 0);
            Timestamp todayDay = new Timestamp(data_odierna.getTimeInMillis());
            boolean today = false;
            if (data_a == null || (data_a.getTime() == todayDay.getTime())) {
                today = true;
            }
            if (!getMessageBox().hasError()) {
                // Comincio ad inserire, in caso, i valori di default
                Calendar data_orario_da = Calendar.getInstance();
                Calendar data_orario_a = Calendar.getInstance();
                int ora_corrente_a = data_orario_a.get(Calendar.HOUR_OF_DAY);
                int minuto_corrente_a = data_orario_a.get(Calendar.MINUTE);

                if (data_da == null) {
                    data_orario_da = null;
                } else {
                    data_orario_da.setTime(data_da);
                }

                if (data_orario_da != null) {
                    if (ore_da == null || minuti_da == null) {
                        data_orario_da.set(Calendar.HOUR_OF_DAY, 0);
                        data_orario_da.set(Calendar.MINUTE, 0);
                        data_orario_da.set(Calendar.SECOND, 0);
                    } else {
                        data_orario_da.set(Calendar.HOUR_OF_DAY, ore_da.intValue());
                        data_orario_da.set(Calendar.MINUTE, minuti_da.intValue());
                        data_orario_da.set(Calendar.SECOND, 0);
                    }
                }

                if (data_a == null) {
                    data_orario_a = null;
                } else {
                    data_orario_a.setTime(data_a);
                }

                if (data_orario_a != null) {
                    if (ore_a != null && minuti_a != null) {
                        data_orario_a.set(Calendar.HOUR_OF_DAY, ore_a.intValue());
                        data_orario_a.set(Calendar.MINUTE, minuti_a.intValue());
                        data_orario_a.set(Calendar.SECOND, 59);
                    } else {
                        if (today) {
                            data_orario_a.set(Calendar.HOUR_OF_DAY, ora_corrente_a);
                            data_orario_a.set(Calendar.MINUTE, minuto_corrente_a);
                        } else {
                            data_orario_a.set(Calendar.HOUR_OF_DAY, 23);
                            data_orario_a.set(Calendar.MINUTE, 59);
                            data_orario_a.set(Calendar.SECOND, 59);
                        }
                    }
                }

                // Controllo che l'ordine delle date ed orari sia corretto
                this.validaOrdineDateOrari(data_orario_da != null ? data_orario_da.getTime() : null,
                        data_orario_a != null ? data_orario_a.getTime() : null, nm_data_da, nm_data_a);

                // Calendar[] dateValidate = new Calendar[2];
                dateValidate[0] = data_orario_da != null ? data_orario_da.getTime() : null;
                dateValidate[1] = data_orario_a != null ? data_orario_a.getTime() : null;
            }
        }
        // Ritorno le date validate
        return dateValidate;
    }

    private void isDateValid(Date data, BigDecimal ore, BigDecimal minuti, String nomeCampo) {
        // Controllo se ho inserito tutte le cifre dell'orario
        if (ore == null) {
            // ora null e minuti presente
            if (minuti != null) {
                getMessageBox().addError("Orario " + nomeCampo + " non corretto: valore Ora assente");
            }
            // ora assente e minuti assente = OK, vengono impostati i valori di
            // default (00:00)
        } else {
            // ora presente e minuti assente
            if (minuti == null) {
                getMessageBox().addError("Orario " + nomeCampo + " non corretto: valore Minuti assente");
            } // ora presente e minuti presente
            else {
                // data assente
                if (data == null) {
                    getMessageBox().addError(nomeCampo + " assente");
                }
                // se anche la data è presente va bene
            }
        }
    }

    public void isTimeValid(BigDecimal ore, BigDecimal minuti, String nomeCampo) {
        if ((ore != null && ore.intValue() > 23) || (minuti != null && minuti.intValue() > 59)) {
            getMessageBox().addError(nomeCampo + " non corretto");
        }
    }

    /**
     * Metodo di validazione delle date inserite nei filtri di ricerca e in fase di inserimento dati
     * 
     * @param data_da
     *            data da
     * @param data_a
     *            data a
     * @param nm_data_da
     *            descrizione campo data da
     * @param nm_data_a
     *            descrizione campo data a
     * 
     * @throws EMFError
     *             errore generico
     */
    public void validaOrdineDateOrari(Date data_da, Date data_a, String nm_data_da, String nm_data_a) throws EMFError {
        // if (data_a != null && data_da == null) {
        // getMessageBox().addError(nm_data_da + " assente");
        // //throw new EMFError(EMFError.WARNING, "Data inizio assente");
        // }
        if (data_a == null && data_da != null) {
            data_a = new Date();
            if (data_a.before(data_da)) {
                data_a = null;
                getMessageBox().addError(nm_data_da + " superiore alla data odierna");
            }
        }
        if (data_da != null && data_a != null) {
            if (data_da.after(data_a)) {
                getMessageBox().addError(nm_data_da + " superiore a " + nm_data_a);
            }
        }
    }
}
