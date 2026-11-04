(function () {
    const csrfToken = document.querySelector('meta[name="_csrf"]').content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;
    const postIdEl = document.getElementById('postId');
    const postId = postIdEl ? postIdEl.value : null;

    $('#content').trumbowyg({
        btns: [
            ['formatting'], ['strong', 'em', 'del'], ['link'],
            ['upload'],
            ['unorderedList', 'orderedList'], ['horizontalRule'],
            ['removeformat'], ['fullscreen']
        ],
        plugins: {
            upload: {
                serverPath: '/api/images/upload',
                fileFieldName: 'file',
                urlPropertyName: 'url',
                data: postId ? [{ name: 'postId', value: postId }] : [],
                headers: { [csrfHeader]: csrfToken }
            }
        }
    });

    const form = document.getElementById('postForm') || document.getElementById('editForm');
    form.addEventListener('submit', function () {
        const html = $('#content').trumbowyg('html');
        const names = new Set();
        let m;
        const regex = /\/images\/([a-zA-Z0-9._-]+)/g;
        while ((m = regex.exec(html)) !== null) names.add(m[1]);
        document.getElementById('imageFileNames').value = Array.from(names).join(',');
    });
})();