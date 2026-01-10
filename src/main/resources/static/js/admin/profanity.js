/**
 * 비속어 관리 페이지 스크립트
 */

// 전체 선택 토글
function toggleCheckAll() {
    const checkAll = document.getElementById('checkAll');
    const checkboxes = document.querySelectorAll('.row-check:not(:disabled)');
    checkboxes.forEach(cb => cb.checked = checkAll.checked);
    updateDeleteButton();
}

// 삭제 버튼 상태 업데이트
function updateDeleteButton() {
    const checkedCount = document.querySelectorAll('.row-check:checked').length;
    const btn = document.getElementById('btnDeleteSelected');
    btn.disabled = checkedCount === 0;
    btn.textContent = checkedCount > 0 ? `🗑️ 선택 삭제 (${checkedCount})` : '🗑️ 선택 삭제';
}

// 모달 열기
function openModal(modalId) {
    document.getElementById(modalId).classList.add('show');
}

// 모달 닫기
function closeModal(modalId) {
    document.getElementById(modalId).classList.remove('show');
}

// 단어 추가 모달 열기
function openAddModal() {
    document.getElementById('modalTitle').textContent = '단어 추가';
    document.getElementById('editWordId').value = '';
    document.getElementById('wordInput').value = '';
    document.getElementById('categoryInput').value = '';
    document.getElementById('descInput').value = '';
    document.getElementById('activeGroup').style.display = 'none';
    openModal('wordModal');
}

// 단어 수정 모달 열기
function openEditModal(id, word, category, description, isActive) {
    document.getElementById('modalTitle').textContent = '단어 수정';
    document.getElementById('editWordId').value = id;
    document.getElementById('wordInput').value = word;
    document.getElementById('categoryInput').value = category || '';
    document.getElementById('descInput').value = description || '';
    document.getElementById('activeInput').checked = isActive;
    document.getElementById('activeGroup').style.display = 'block';
    openModal('wordModal');
}

// 일괄 추가 모달 열기
function openBulkAddModal() {
    document.getElementById('bulkCategory').value = '';
    document.getElementById('bulkWords').value = '';
    openModal('bulkAddModal');
}

// 테스트 모달 열기
function openTestModal() {
    document.getElementById('testInput').value = '';
    document.getElementById('testResult').style.display = 'none';
    openModal('testModal');
}

// 단어 저장
async function saveWord() {
    const id = document.getElementById('editWordId').value;
    const word = document.getElementById('wordInput').value.trim();
    const category = document.getElementById('categoryInput').value;
    const description = document.getElementById('descInput').value.trim();
    const isActive = document.getElementById('activeInput').checked;

    if (!word) {
        alert('단어를 입력해주세요.');
        return;
    }

    const data = { word, category, description };
    if (id) {
        data.isActive = isActive;
    }

    try {
        const url = id 
            ? `${contextPath}/api/admin/profanity/${id}` 
            : `${contextPath}/api/admin/profanity`;
        
        const response = await fetch(url, {
            method: id ? 'PUT' : 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });

        const result = await response.json();
        
        if (result.success) {
            alert(result.message);
            closeModal('wordModal');
            location.reload();
        } else {
            alert(result.message || '처리에 실패했습니다.');
        }
    } catch (error) {
        console.error('Error:', error);
        alert('처리 중 오류가 발생했습니다.');
    }
}

// 단어 삭제
async function deleteWord(id) {
    if (!confirm('이 단어를 삭제하시겠습니까?')) return;

    try {
        const response = await fetch(`${contextPath}/api/admin/profanity/${id}`, {
            method: 'DELETE'
        });

        const result = await response.json();
        
        if (result.success) {
            alert(result.message);
            location.reload();
        } else {
            alert(result.message || '삭제에 실패했습니다.');
        }
    } catch (error) {
        console.error('Error:', error);
        alert('삭제 중 오류가 발생했습니다.');
    }
}

// 선택 삭제
async function deleteSelected() {
    const checkedBoxes = document.querySelectorAll('.row-check:checked');
    if (checkedBoxes.length === 0) {
        alert('삭제할 항목을 선택해주세요.');
        return;
    }

    if (!confirm(`선택한 ${checkedBoxes.length}개 단어를 삭제하시겠습니까?`)) return;

    const ids = Array.from(checkedBoxes).map(cb => parseInt(cb.value));

    try {
        const response = await fetch(`${contextPath}/api/admin/profanity/delete-multiple`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ ids })
        });

        const result = await response.json();
        
        if (result.success) {
            alert(result.message);
            location.reload();
        } else {
            alert(result.message || '삭제에 실패했습니다.');
        }
    } catch (error) {
        console.error('Error:', error);
        alert('삭제 중 오류가 발생했습니다.');
    }
}

// 일괄 추가 저장
async function saveBulkWords() {
    const category = document.getElementById('bulkCategory').value;
    const wordsText = document.getElementById('bulkWords').value.trim();

    if (!wordsText) {
        alert('단어를 입력해주세요.');
        return;
    }

    // 줄바꿈 또는 쉼표로 분리
    const words = wordsText
        .split(/[\n,]+/)
        .map(w => w.trim())
        .filter(w => w.length > 0);

    if (words.length === 0) {
        alert('유효한 단어가 없습니다.');
        return;
    }

    try {
        const response = await fetch(`${contextPath}/api/admin/profanity/add-multiple`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ words, category })
        });

        const result = await response.json();
        
        if (result.success) {
            alert(result.message);
            closeModal('bulkAddModal');
            location.reload();
        } else {
            alert(result.message || '추가에 실패했습니다.');
        }
    } catch (error) {
        console.error('Error:', error);
        alert('추가 중 오류가 발생했습니다.');
    }
}

// 상태 토글
async function toggleStatus(id) {
    try {
        const response = await fetch(`${contextPath}/api/admin/profanity/${id}/toggle`, {
            method: 'POST'
        });

        const result = await response.json();
        
        if (result.success) {
            location.reload();
        } else {
            alert(result.message || '상태 변경에 실패했습니다.');
        }
    } catch (error) {
        console.error('Error:', error);
        alert('상태 변경 중 오류가 발생했습니다.');
    }
}

// 캐시 갱신
async function refreshCache() {
    try {
        const response = await fetch(`${contextPath}/api/admin/profanity/refresh-cache`, {
            method: 'POST'
        });

        const result = await response.json();
        alert(result.message);
    } catch (error) {
        console.error('Error:', error);
        alert('캐시 갱신 중 오류가 발생했습니다.');
    }
}

// 기본 데이터 초기화
async function initializeDefault() {
    if (!confirm('기본 비속어 데이터를 초기화하시겠습니까?\n약 200개의 기본 단어가 등록됩니다.')) return;

    try {
        const response = await fetch(`${contextPath}/api/admin/profanity/initialize`, {
            method: 'POST'
        });

        const result = await response.json();
        
        if (result.success) {
            alert(result.message);
            location.reload();
        } else {
            alert(result.message);
        }
    } catch (error) {
        console.error('Error:', error);
        alert('초기화 중 오류가 발생했습니다.');
    }
}

// 테스트 실행
async function runTest() {
    const text = document.getElementById('testInput').value.trim();
    const resultDiv = document.getElementById('testResult');

    if (!text) {
        alert('테스트할 텍스트를 입력해주세요.');
        return;
    }

    try {
        const response = await fetch(`${contextPath}/api/admin/profanity/test`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ text })
        });

        const result = await response.json();

        resultDiv.style.display = 'block';

        if (result.hasProfanity) {
            let detailsHtml = '';
            if (result.detectedDetails && result.detectedDetails.length > 0) {
                detailsHtml = '<div class="detected-details"><h5>상세 감지 정보:</h5><ul>';
                result.detectedDetails.forEach(detail => {
                    detailsHtml += `<li>
                        <span class="detail-word">"${detail.word}"</span>
                        <span class="detail-type">[${detail.type}]</span>
                        <span class="detail-context">${detail.context}</span>
                    </li>`;
                });
                detailsHtml += '</ul></div>';
            }

            resultDiv.className = 'test-result error';
            resultDiv.innerHTML = `
                <h4>⚠️ 비속어 감지됨</h4>
                <p><strong>감지된 단어:</strong> ${result.detectedWords.join(', ')}</p>
                ${detailsHtml}
                <p><strong>필터링 결과:</strong> ${result.filteredText}</p>
            `;
        } else {
            resultDiv.className = 'test-result success';
            resultDiv.innerHTML = `
                <h4>✅ 비속어 없음</h4>
                <p>입력한 텍스트에서 비속어가 감지되지 않았습니다.</p>
            `;
        }
    } catch (error) {
        console.error('Error:', error);
        resultDiv.style.display = 'block';
        resultDiv.className = 'test-result error';
        resultDiv.innerHTML = '<h4>❌ 테스트 실패</h4><p>오류가 발생했습니다.</p>';
    }
}

// 필터 초기화
function resetFilter() {
    document.getElementById('filterCategory').value = '';
    document.getElementById('filterKeyword').value = '';
    document.getElementById('searchForm').submit();
}

// ESC 키로 모달 닫기
document.addEventListener('keydown', function(e) {
    if (e.key === 'Escape') {
        document.querySelectorAll('.modal.show').forEach(modal => {
            modal.classList.remove('show');
        });
    }
});

// 모달 외부 클릭 시 닫기
document.querySelectorAll('.modal').forEach(modal => {
    modal.addEventListener('click', function(e) {
        if (e.target === this) {
            this.classList.remove('show');
        }
    });
});
