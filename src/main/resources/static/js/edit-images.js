function initPostEditor(options) {
    const csrfMeta = document.querySelector('meta[name="_csrf"]');
    const csrfHeaderMeta = document.querySelector('meta[name="_csrf_header"]');
    const {
        csrfInput = document.querySelector('input[name="_csrf"]'),
        csrfToken = csrfMeta?.content || csrfInput?.value,
        csrfHeader = csrfHeaderMeta?.content || "X-XSRF-TOKEN",
        textareaId,
        draftIdSelector,
        existingImagesSelector = null,
        removedInputSelector = null,
        redirectOnCancel,
        normalize = url => new URL(url, window.location.origin).pathname,
        uploadedImages = new Map(),
        existingImages = new Map(),
        removedExisting = new Set(),
        deleteEndpoint = "/api/images/",
        uploadEndpoint = "/api/images/upload"
    } = options;

    async function readErrorResponse(res) {
        try {
            const text = await res.text();
            return text || `HTTP ${res.status}`;
        } catch (err) {
            return `HTTP ${res.status}`;
        }
    }

    async function deleteImageById(id) {
        const res = await fetch(`${deleteEndpoint}${id}`, {
            method: "DELETE",
            headers: { [csrfHeader]: csrfToken }
        });
        if (!res.ok) {
            throw new Error(`Не удалось удалить изображение ${id}: ${await readErrorResponse(res)}`);
        }
    }

    if (existingImagesSelector) {
        document.querySelectorAll(existingImagesSelector).forEach(img => {
            existingImages.set(normalize(img.src), Number(img.dataset.id));
        });
    }

    const collectImages = editor =>
        new Set([...editor.getBody().querySelectorAll("img")].map(i => normalize(i.src)));

    tinymce.init({
        license_key: 'gpl',
        selector: `#${textareaId}`,
        height: 520,
        menubar: false,
        plugins: 'image link lists code',
        toolbar: `
                formatselect | bold italic underline | h1 h2 h3 h4 h5 |
                inlinecode | bullist numlist | link image | code
                `,
        automatic_uploads: true,
        relative_urls: false,
        remove_script_host: true,
        setup: editor => {
            editor.ui.registry.addToggleButton('inlinecode', {
                icon: 'sourcecode',
                tooltip: 'Inline code (Ctrl+E)',
                onAction: () => {
                    editor.execCommand('mceToggleFormat', false, 'code');
                },
                onSetup: api => {
                    const handler = () => {
                        api.setActive(editor.formatter.match('code'));
                    };
                    editor.on('NodeChange', handler);
                    return () => editor.off('NodeChange', handler);
                }
            });

            editor.addShortcut('ctrl+e', 'Toggle inline code', () => {
                editor.execCommand('mceToggleFormat', false, 'code');
            });
        },

        images_upload_handler: async blobInfo => {
            const fd = new FormData();
            fd.append("file", blobInfo.blob());
            fd.append("draftId", document.querySelector(draftIdSelector).value);

            const res = await fetch(uploadEndpoint, {
                method: "POST",
                headers: { [csrfHeader]: csrfToken },
                body: fd
            });
            if (!res.ok) {
                throw new Error(`Не удалось загрузить изображение: ${await readErrorResponse(res)}`);
            }

            const json = await res.json();
            uploadedImages.set(json.url, json.id);
            return json.url;
        },
    });

    const form = document.querySelector("form");

    if (form) {
        form.addEventListener("submit", async (e) => {
            const editor = tinymce.get(textareaId);
            const currentImages = collectImages(editor);

            existingImages.forEach((id, url) => {
                if (!currentImages.has(url)) {
                    removedExisting.add(id);
                }
            });

            for (const [url, id] of uploadedImages.entries()) {
                if (!currentImages.has(url)) {
                    try {
                        await deleteImageById(id);
                        uploadedImages.delete(url);
                    } catch (err) {
                        console.error("Ошибка удаления изображения:", id, err);
                    }
                }
            }

            if (removedInputSelector) {
                document.querySelector(removedInputSelector).value =
                    [...removedExisting].join(",");
            }
        });
    }

    document.getElementById("cancelBtn")?.addEventListener("click", async () => {
        if (!confirm("Отменить? Все изменения будут потеряны.")) return;

        for (const id of uploadedImages.values()) {
            try {
                await deleteImageById(id);
            } catch (err) {
                console.error("Ошибка удаления изображения при отмене:", id, err);
            }
        }

        window.location.href = redirectOnCancel;
    });
}