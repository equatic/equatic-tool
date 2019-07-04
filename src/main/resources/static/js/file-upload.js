$(document).on('change', '.btn-file :file', function () {
    var input = $(this),
        label = input.val().replace(/\\/g, '/').replace(/.*\//, '');
    input.trigger('fileselect', label);
});

$(document).ready(function () {
    $('.btn-file :file').on('fileselect', function (event, label) {
        $(this).parents('.input-group').find(':text').val(label);
    });

    $('.btn-upload').on('click', function () {
        $(this).button('loading');
    })
});