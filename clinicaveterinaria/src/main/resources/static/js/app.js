class AppVacinacao {

    static CONFIG = {
        seletores: {
            money: ".componentemoney",
            data: ".componentedata",
            dataManual: ".componentedatamanual",
            hora: ".componentehora",
            cpf: ".componentecpf",
            confirm: "[hx-confirm]:not([data-confirm-ready])",
            buscaContainer: ".searchable-select-container:not([data-initialized])",
            senha: ".toggle-senha",
        },
        maskMoney: {
            prefix: "",
            suffix: "",
            fixed: true,
            fractionDigits: 2,
            decimalSeparator: ",",
            thousandsSeparator: ".",
            autoCompleteDecimal: true,
        },
        datePicker: {
            dateFormat: "dd/mm/yyyy",
            customWeekDays: ["Domingo", "Segunda", "Terça", "Quarta", "Quinta", "Sexta", "Sábado"],
            customMonths: ["Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho", "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"],
            customClearBTN: "Limpar",
            customCancelBTN: "Cancelar",
            theme: { theme_color: "rgb(31 41 55)" },
        },
        texto: {
            cancelar: "Cancelar",
            remover: "Remover",
            tituloConfirm: "Você tem certeza?",
        }
    };

    static Toast = Swal.mixin({
        toast: true,
        position: "center-end",
        iconColor: "white",
        customClass: { popup: "colored-toast" },
        showConfirmButton: false,
        timer: 3000,
        timerProgressBar: true,
        grow: "row",
        didOpen: (toast) => {
            toast.onmouseenter = Swal.stopTimer;
            toast.onmouseleave = Swal.resumeTimer;
        },
    });

    static csrfData = {
        header: null,
        token: null
    };

    static init() {
        console.log("[DEBUG] init chamado");
        this.lerCsrfDoHTML();
        // this.atualizarComponentes(); // Removido pois o htmx.onLoad chamará para o document inicial
        this.registrarListenersGlobais();
    }

    static atualizarComponentes(container = document) {
        console.log("[DEBUG] atualizarComponentes chamado.");
        this.prepararMoney(container);
        this.prepararData(container);
        this.prepararHora(container);
        this.prepararConfirmacoes(container);
        this.prepararBusca(container);
        this.prepararCpf(container);
        this.prepararSenha(container);
        this.prepararNumero(container);
    }

    static lerCsrfDoHTML() {
        const tokenMeta = document.querySelector('meta[name="_csrf"]');
        const headerMeta = document.querySelector('meta[name="_csrf_header"]');
        if (tokenMeta && headerMeta) {
            this.csrfData.token = tokenMeta.content;
            this.csrfData.header = headerMeta.content;
        }
    }

    static registrarListenersGlobais() {
        if (typeof htmx !== 'undefined') {
            htmx.onLoad((elt) => {
                console.log("[DEBUG] htmx.onLoad disparado.");
                this.atualizarComponentes(elt);
            });

            htmx.on("htmx:after:swap", (evt) => {
                console.log("[DEBUG] htmx:after:swap disparado.");
                // Lógica de Captura do CSRF (vinda da resposta)
                if (evt.detail.xhr) {
                    let header = "X-CSRF-TOKEN";
                    let token = evt.detail.xhr.getResponseHeader(header);
                    if (token == null) {
                        header = "X-XSRF-TOKEN";
                        token = evt.detail.xhr.getResponseHeader(header);
                    }
                    if (token) {
                        this.csrfData.header = header;
                        this.csrfData.token = token;
                    }
                }
            });
        }

        // document.body.addEventListener("htmx:config:request", (evt) => {
        //     evt.detail.headers["accept"] = "text/html-partial";
        //     if (evt.detail.verb !== "get") {
        //         if (this.csrfData.header != null && this.csrfData.token != null) {
        //             evt.detail.headers[this.csrfData.header] = this.csrfData.token;
        //         }
        //     }
        //     // Converte datas para ISO e remove formatações de números antes do envio
        //     this._normalizarParametrosParaEnvio(evt.detail.parameters);
        // });

        document.body.addEventListener("htmx:config:request", (evt) => {
            // Suporta tanto o HTMX com contexto (ctx) quanto o HTMX padrão
            const ctx = evt.detail.ctx;
            const request = ctx ? ctx.request : null;
            const headers = request ? request.headers : evt.detail.headers;
            const verb = (request ? request.method : evt.detail.verb || "").toLowerCase();

            if (headers) {
                headers["accept"] = "text/html-partial";
                if (verb !== "get") {
                    if (this.csrfData.header != null && this.csrfData.token != null) {
                        headers[this.csrfData.header] = this.csrfData.token;
                    }
                }
            }

            // 1. Se for HTMX com FormData (como o seu htmx.min.js atual)
            if (request && request.body instanceof FormData) {
                for (const [key, val] of Array.from(request.body.entries())) {
                    const valLimpo = this._sanitizarValor(val);
                    if (valLimpo !== val) {
                        request.body.set(key, valLimpo);
                    }
                }
            }

            // 2. Se for HTMX padrão com objeto de parâmetros
            if (evt.detail.parameters) {
                this._normalizarParametrosParaEnvio(evt.detail.parameters);
            }
        });

        document.body.addEventListener("exibirAlerta", (evt) => {
            this.mostrarNotificacaoViaHeader(evt.detail);
        });

        if (!document.body.dataset.clickOutsideRegistered) {
            document.addEventListener("click", (event) => {
                const activeDropdown = document.querySelector(".custom-select-dropdown:not(.hidden)");
                if (activeDropdown && !event.target.closest(".searchable-select-container")) {
                    activeDropdown.classList.add("hidden");
                    const activeTrigger = activeDropdown.closest(".searchable-select-container").querySelector(".custom-select-trigger");
                    if (activeTrigger) {
                        this._removeFocus(activeTrigger);
                    }
                }
            });
            document.body.dataset.clickOutsideRegistered = "true";
        }
    }

    static prepararMoney(container = document) {
        let inputs = container.querySelectorAll(this.CONFIG.seletores.money);
        if (container.matches && container.matches(this.CONFIG.seletores.money)) {
            inputs = [container];
        }
        console.log(`[DEBUG] prepararMoney encontrou ${inputs.length} elementos.`);

        inputs.forEach((input) => {
            if (typeof SimpleMaskMoney !== 'undefined') {
                SimpleMaskMoney.setMask(input, this.CONFIG.maskMoney);
            }
            input.classList.remove("componentemoney");
        });
    }

    static prepararData(container = document) {
        let inputs = container.querySelectorAll(this.CONFIG.seletores.data);
        if (container.matches && container.matches(this.CONFIG.seletores.data)) {
            inputs = [container];
        }

        inputs.forEach((input) => {
            // Variável para guardar a referência do calendário
            let datePicker = null;

            // Função local para sincronizar o texto com o calendário
            const sincronizarCalendario = () => {
                if (datePicker && input.value.length === 10) {
                    const parts = input.value.split("/");
                    const d = new Date(+parts[2], parts[1] - 1, +parts[0]);
                    // Garante que é uma data válida antes de mudar o calendário
                    if (!isNaN(d.getTime())) {
                        datePicker.setFullDate(d);
                    }
                }
            };

            let apagando = false;
            input.addEventListener("keydown", (e) => { apagando = e.key === "Backspace"; });

            input.addEventListener("input", (e) => {
                if (apagando) return;
                let v = e.target.value.replace(/\D/g, "");
                if (v.length > 2 && v.length <= 4) {
                    v = v.substring(0, 2) + "/" + v.substring(2);
                } else if (v.length > 4) {
                    v = v.substring(0, 2) + "/" + v.substring(2, 4) + "/" + v.substring(4, 8);
                }
                e.target.value = v;

                // Tenta sincronizar instantaneamente se a data estiver completa
                sincronizarCalendario();
            });

            // Sincroniza também caso o usuário saia do campo ou cole um valor
            input.addEventListener("blur", sincronizarCalendario);

            input.classList.remove("componentedata");
            input.setAttribute("placeholder", "DD/MM/AAAA");
            input.setAttribute("maxlength", "10");

            // --- CALENDÁRIO VINCULADO AO ÍCONE ---
            if (typeof MCDatepicker !== 'undefined') {
                const btnId = "btn-" + input.id;
                const btn = (container.querySelector ? container.querySelector("#" + btnId) : null)
                    || document.getElementById(btnId);

                if (btn) {
                    // 1. Cria uma "âncora" invisível dentro do botão
                    const anchorId = "anchor-" + input.id;
                    let anchor = document.getElementById(anchorId);

                    if (!anchor) {
                        anchor = document.createElement("span");
                        anchor.id = anchorId;
                        // Fica invisível, não atrapalha o layout e não recebe foco do TAB
                        anchor.style.cssText = "position: absolute; width: 1px; height: 1px; opacity: 0; pointer-events: none;";
                        btn.appendChild(anchor);
                    }

                    // 2. Vincula o MCDatepicker à âncora fantasma (isso resolve a posição na tela)
                    datePicker = MCDatepicker.create({
                        ...this.CONFIG.datePicker,
                        el: "#" + anchor.id,
                        autoClose: false
                    });

                    // 3. Controle Manual: Dispara com Click
                    btn.addEventListener("click", (e) => {
                        e.preventDefault();
                        datePicker.open();
                    });

                    // 4. Controle Manual: Dispara com Espaço ou Enter (padrão de acessibilidade)
                    btn.addEventListener("keydown", (e) => {
                        if (e.key === " " || e.key === "Enter") {
                            e.preventDefault(); // Evita que a barra de espaço role a página para baixo
                            datePicker.open();
                        }
                    });

                    datePicker.onSelect((date, formatedDate) => {
                        input.value = formatedDate;

                        // Dispara o evento de input para garantir que o HTMX/Validação percebam a mudança
                        input.dispatchEvent(new Event('input', { bubbles: true }));
                    });

                    sincronizarCalendario();
                }
            }
        });
    }

    static prepararHora(container = document) {
        let inputs = container.querySelectorAll(this.CONFIG.seletores.hora);
        if (container.matches && container.matches(this.CONFIG.seletores.hora)) {
            inputs = [container];
        }
        console.log(`[DEBUG] prepararHora encontrou ${inputs.length} elementos.`);

        inputs.forEach((input) => {
            let apagando = false;

            // 1. Lógica de Máscara (Teclado)
            input.addEventListener("keydown", (e) => {
                apagando = e.key === "Backspace";
            });

            input.addEventListener("input", (e) => {
                if (apagando) return;

                let v = e.target.value.replace(/\D/g, "");

                if (v.length > 2) {
                    v = v.substring(0, 2) + ":" + v.substring(2, 4);
                }

                e.target.value = v;
            });

            input.classList.remove("componentehora");
            input.setAttribute("placeholder", "HH:MM");
            input.setAttribute("maxlength", "5");

            // 2. Acoplando o relógio visual
            if (typeof mdDateTimePicker !== 'undefined') {
                const btnId = "btn-" + input.id;
                const btn = (container.querySelector ? container.querySelector("#" + btnId) : null)
                    || document.getElementById(btnId);

                const dialog = new mdDateTimePicker.default({
                    type: "time", mode: true, inner24: true, cancel: this.CONFIG.texto.cancelar,
                });

                const sincronizarRelogio = () => {
                    if (input.value && input.value.length === 5) {
                        // Validação básica para evitar que o relógio quebre com horas bizarras (ex: 99:99)
                        const partes = input.value.split(':');
                        const h = parseInt(partes[0], 10);
                        const m = parseInt(partes[1], 10);

                        if (h >= 0 && h <= 23 && m >= 0 && m <= 59) {
                            if (typeof moment !== 'undefined') {
                                // Atualiza o objeto Moment interno da biblioteca
                                dialog.time = moment(input.value, "HH:mm");
                            }
                        }
                    }
                };

                if (btn) {
                    btn.addEventListener("click", (e) => {
                        e.preventDefault();
                        sincronizarRelogio(); // <--- Lê a hora digitada antes de abrir
                        dialog.toggle();
                    });

                    btn.addEventListener("keydown", (e) => {
                        if (e.key === " " || e.key === "Enter") {
                            e.preventDefault();
                            sincronizarRelogio(); // <--- Lê a hora digitada antes de abrir
                            dialog.toggle();
                        }
                    });
                }

                dialog.trigger = input;

                input.addEventListener("onOk", () => {
                    input.value = dialog.time.format("HH:mm");
                    input.dispatchEvent(new Event('input', { bubbles: true }));
                });
            }
        });
    }

    static prepararCpf(container = document) {
        let inputs = container.querySelectorAll(this.CONFIG.seletores.cpf);
        if (container.matches && container.matches(this.CONFIG.seletores.cpf)) {
            inputs = [container];
        }

        // Função interna para aplicar a máscara no texto
        const formatarCPF = (valor) => {
            if (!valor) return "";
            let v = valor.replace(/\D/g, ""); // Remove tudo que não é número

            if (v.length > 9) {
                return v.replace(/(\d{3})(\d{3})(\d{3})(\d{1,2})/, "$1.$2.$3-$4");
            } else if (v.length > 6) {
                return v.replace(/(\d{3})(\d{3})(\d{1,3})/, "$1.$2.$3");
            } else if (v.length > 3) {
                return v.replace(/(\d{3})(\d{1,3})/, "$1.$2");
            }
            return v;
        };

        inputs.forEach((input) => {
            // 1. Formata o valor que veio do backend (ex: "12345678900" -> "123.456.789-00")
            if (input.value) {
                input.value = formatarCPF(input.value);
            }

            let apagando = false;

            input.addEventListener("keydown", (e) => {
                apagando = e.key === "Backspace";
            });

            // 2. Formata enquanto o usuário digita
            input.addEventListener("input", (e) => {
                if (apagando) return;
                e.target.value = formatarCPF(e.target.value);
            });

            // Configurações nativas do input
            input.classList.remove("componentecpf");
            input.setAttribute("placeholder", "000.000.000-00");
            input.setAttribute("maxlength", "14");
        });
    }

    static prepararSenha(container = document) {
        let botoes = container.querySelectorAll(this.CONFIG.seletores.senha);
        if (container.matches && container.matches(this.CONFIG.seletores.senha)) {
            botoes = [container];
        }

        botoes.forEach(btn => {
            btn.addEventListener("click", (e) => {
                e.preventDefault();

                // 1. Acha o container relative pai
                const containerRelativo = btn.closest(".relative");

                // 2. Acha o input de texto/senha (ignorando o hidden)
                const input = containerRelativo.querySelector("input:not([type='hidden'])");

                // 3. Acha os ícones
                const olhoAberto = btn.querySelector(".icone-olho-aberto");
                const olhoFechado = btn.querySelector(".icone-olho-fechado");

                // 4. Faz a troca lógica (Toggle)
                if (input.type === "password") {
                    input.type = "text";
                    olhoFechado.classList.add("hidden");
                    olhoAberto.classList.remove("hidden");
                } else {
                    input.type = "password";
                    olhoAberto.classList.add("hidden");
                    olhoFechado.classList.remove("hidden");
                }
            });
        });
    }

    static prepararNumero(container = document) {
        const inputs = container.querySelectorAll(".input-numero");

        inputs.forEach(input => {
            const isDecimal = input.dataset.decimal === "true";
            const showGroups = input.dataset.groups === "true";

            // Formatação inicial quando a página carrega
            const formatar = (valor) => {
                if (!valor) return "";
                let v = valor.toString();

                // 1. O valor já veio com vírgula do Spring (ex: 1.234,56)?
                if (v.includes(',')) {
                    // Remove todos os pontos de milhar para recalcular depois
                    v = v.replace(/\./g, "");
                }
                // 2. O valor veio "cru" do Java (ex: 1234.56)?
                else if (isDecimal && v.includes('.')) {
                    // Troca o ponto decimal por vírgula
                    v = v.replace('.', ',');
                }

                // Remove qualquer lixo que tenha sobrado, deixando só números e vírgula
                v = v.replace(/[^0-9,]/g, "");

                // Reaplica a máscara de grupos (pontos de milhar)
                if (showGroups) {
                    let partes = v.split(',');
                    partes[0] = partes[0].replace(/\B(?=(\d{3})+(?!\d))/g, ".");
                    v = partes.join(',');
                }

                return v;
            };

            if (input.value) {
                input.value = formatar(input.value);
            }

            // Máscara enquanto o aluno digita
            input.addEventListener("input", (e) => {
                let v = e.target.value.replace(/[^0-9,]/g, ""); // Permite só números e vírgula

                // Impede mais de uma vírgula
                if ((v.match(/,/g) || []).length > 1) {
                    v = v.substring(0, v.lastIndexOf(','));
                }

                // Se não for decimal, remove qualquer vírgula que tenha passado
                if (!isDecimal) {
                    v = v.replace(/,/g, "");
                }

                if (showGroups) {
                    let partes = v.split(',');
                    partes[0] = partes[0].replace(/\D/g, "").replace(/\B(?=(\d{3})+(?!\d))/g, ".");
                    v = partes.join(',');
                }
                e.target.value = v;
            });
        });
    }

    static prepararConfirmacoes(container = document) {
        let elementos = container.querySelectorAll(this.CONFIG.seletores.confirm);
        if (container.matches && container.matches(this.CONFIG.seletores.confirm)) {
            elementos = [container];
        }
        console.log(`[DEBUG] prepararConfirmacoes encontrou ${elementos.length} elementos.`);

        elementos.forEach((elemento) => {
            elemento.addEventListener("htmx:confirm", (e) => this._dispararSweetAlert(e));
            elemento.setAttribute("data-confirm-ready", "true");
        });
    }

    static _dispararSweetAlert(e) {
        e.preventDefault();
        Swal.fire({
            title: AppVacinacao.CONFIG.texto.tituloConfirm,
            text: e.detail.question,
            icon: "warning",
            showCancelButton: true,
            cancelButtonText: AppVacinacao.CONFIG.texto.cancelar,
            confirmButtonText: AppVacinacao.CONFIG.texto.remover,
            confirmButtonColor: "#3085d6",
        }).then((result) => {
            if (result.isConfirmed) {
                e.detail.issueRequest(true);
            }
        });
    }

    static mostrarNotificacaoViaHeader(dados) {
        if (dados) {
            this.Toast.fire({ icon: dados.tipo, title: dados.mensagem, timer: dados.intervalo || 3000 });
        }
    }

    static _applyFocus(trigger) {
        trigger.classList.remove("border-gray-300");
        trigger.classList.add("ring-1", "border-gray-500", "ring-gray-500");
    }

    static _removeFocus(trigger) {
        trigger.classList.remove("ring-1", "border-gray-500", "ring-gray-500");
        trigger.classList.add("border-gray-300");
    }

    static _sanitizarValor(val) {
        if (typeof val !== "string") return val;
        val = val.trim();

        // 1. Data brasileira (DD/MM/AAAA) -> ISO (AAAA-MM-DD)
        if (/^\d{2}\/\d{2}\/\d{4}$/.test(val)) {
            const [dia, mes, ano] = val.split("/");
            return `${ano}-${mes}-${dia}`;
        }

        // 2. Dinheiro ou Decimal BR (ex: 1.234,56 ou 12,50) -> Float puro (1234.56 ou 12.50)
        if (/^\d{1,3}(\.\d{3})*,\d{1,2}$/.test(val)) {
            return val.replace(/\./g, "").replace(",", ".");
        }

        // 3. Inteiro com separador de milhar (ex: código "1.234") -> Inteiro puro ("1234")
        if (/^\d{1,3}(\.\d{3})+$/.test(val)) {
            return val.replace(/\./g, "");
        }

        // 4. Limpa o CPF formatado (000.000.000-00 -> 00000000000)
        if (/^\d{3}\.\d{3}\.\d{3}-\d{2}$/.test(val)) {
            return val.replace(/\D/g, "");
        }

        return val;
    }

    static _normalizarParametrosParaEnvio(params) {
        if (!params) return;
        for (const key in params) {
            const valor = params[key];
            if (Array.isArray(valor)) {
                params[key] = valor.map(v => this._sanitizarValor(v));
            } else {
                params[key] = this._sanitizarValor(valor);
            }
        }
    }

    static prepararBusca(container = document) {
        let containers = container.querySelectorAll(this.CONFIG.seletores.buscaContainer);
        if (container.matches && container.matches(this.CONFIG.seletores.buscaContainer)) {
            containers = [container];
        }
        console.log(`[DEBUG] prepararBusca encontrou ${containers.length} elementos.`);

        containers.forEach((container) => {
            container.dataset.initialized = "true";
            const trigger = container.querySelector(".custom-select-trigger");
            const dropdown = container.querySelector(".custom-select-dropdown");
            const optionsList = container.querySelector(".options-list");
            const selectedValueSpan = container.querySelector(".selected-value-span");
            const hiddenInput = container.querySelector(".hidden-select-input");

            if (trigger && dropdown) {
                trigger.addEventListener("click", (e) => {
                    const isCurrentlyOpen = !dropdown.classList.contains("hidden");
                    document.querySelectorAll(".custom-select-dropdown:not(.hidden)").forEach(d => {
                        if (d !== dropdown) {
                            d.classList.add("hidden");
                            const otherTrigger = d.closest(".searchable-select-container")?.querySelector(".custom-select-trigger");
                            if (otherTrigger) {
                                this._removeFocus(otherTrigger);
                            }
                        }
                    });
                    dropdown.classList.toggle("hidden");
                    if (!isCurrentlyOpen) {
                        this._applyFocus(trigger);
                    } else {
                        this._removeFocus(trigger);
                    }
                    e.stopPropagation();
                    if (!isCurrentlyOpen) {
                        const searchInput = dropdown.querySelector('input[type="text"]');
                        if (searchInput) {
                            searchInput.focus();
                        }
                    }
                });
            }

            if (optionsList) {
                optionsList.addEventListener("click", (event) => {
                    const option = event.target.closest("li");
                    if (option) {
                        if (selectedValueSpan) {
                            selectedValueSpan.textContent = option.dataset.text || option.textContent.trim();
                        }
                        if (hiddenInput) hiddenInput.value = option.dataset.value;
                        if (dropdown) dropdown.classList.add("hidden");
                        const trigger = container.querySelector(".custom-select-trigger");
                        if (trigger) {
                            AppVacinacao._removeFocus(trigger);
                        }
                    }
                });
            }
        });
    }
}

// Inicialização automática
document.addEventListener("DOMContentLoaded", () => AppVacinacao.init());
