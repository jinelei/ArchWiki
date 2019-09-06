function findAllLanguage(){
    var allLanguageNodeList = document.querySelectorAll('a.interlanguage-link-target');
    return Array.from(allLanguageNodeList).flatMap(ele => ele.lang + '^' + ele.innerText + '^' + ele.href);
};
function autoHideElement() {
    var eles = ['div#p-lang', 'div#mw-navigation',
        'div#mw-head-base', 'div#mw-page-base',
        'div.archwiki-template-meta-related-articles-start',
        'div#archnavbar', 'div#footer'];
    for (var i in eles) {
        var temp = document.querySelector(eles[i]);
        if (!!temp) {
            temp.style.display = 'none';
        }
    }
};
function searchKey(key){
    document.querySelector('input#searchInput').value = key;
    document.querySelector('input#searchButton').click();
};
function findAllRelatedArticles(){
    var eles = document.querySelectorAll('div.archwiki-template-meta-related-articles-start > ul > li > a');
    return Array.from(eles).flatMap(ele => ele.innerText + '^' + ele.href);
};
