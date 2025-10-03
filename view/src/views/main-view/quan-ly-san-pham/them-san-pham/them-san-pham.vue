<template>
  <div class="add-product-page">
    <!-- Breadcrumb - Changed to add variants only, not create new products -->
    <Breadcrumb :items="breadcrumbItems" />

    <!-- Product Form -->
    <a-card title="Thêm biến thể sản phẩm" class="product-form-card">
      <a-form :model="formData" :rules="formRules" ref="formRef" layout="vertical">
        <!-- Basic Information -->
        <!-- Product Name - Full Width Row -->
        <a-row :gutter="16">
          <a-col :span="24">
            <a-form-item>
              <template #label>
                <div class="label-with-add-btn">
                  <span class="required-field">Tên sản phẩm</span>
                </div>
              </template>
              <div class="product-name-autocomplete">
                <a-input
                  v-model="formData.name"
                  placeholder="Nhập tên sản phẩm"
                  class="product-name-input"
                  @input="handleProductNameInput"
                  @focus="() => handleProductNameInput(formData.name || '')"
                  @blur="handleProductNameBlur"
                />

                <!-- New Product Indicator -->
                <div v-if="formData.selectedProductId === 'pending' && pendingNewProduct" class="new-product-indicator">
                  <a-tag color="green" size="small">
                    <template #icon>
                      <icon-plus />
                    </template>
                    Sẽ tạo sản phẩm mới: {{ pendingNewProduct.tenSanPham }}
                  </a-tag>
                </div>

                <div v-if="showProductNameInputs" class="autocomplete-dropdown">
                  <div v-for="input in filteredProductInputs" :key="input.id" class="autocomplete-input" @click="selectProductName(input)">
                    <div class="input-name">{{ input.label }}</div>
                    <div class="input-details">
                      <span v-if="input.nhaSanXuat">{{ input.nhaSanXuat.tenNhaSanXuat }}</span>
                      <span v-if="input.xuatXu">- {{ input.xuatXu.tenXuatXu }}</span>
                    </div>
                  </div>
                  <div v-if="filteredProductInputs.length === 0" class="no-inputs">Không tìm thấy sản phẩm phù hợp</div>
                </div>
              </div>
            </a-form-item>
          </a-col>
        </a-row>

        <!-- Manufacturer and Origin Row -->
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item>
              <template #label>
                <div class="label-with-add-btn">
                  <span class="required-field">Nhà sản xuất</span>
                </div>
              </template>
              <div class="search-input-container">
                <a-input
                  v-model="manufacturerSearchValue"
                  placeholder="Nhập để tìm kiếm nhà sản xuất"
                  class="search-input"
                  @input="handleManufacturerInput"
                  @focus="() => handleManufacturerInput(manufacturerSearchValue || '')"
                  @blur="handleManufacturerBlur"
                />
                <a-button type="text" size="mini" @click="showAddManufacturerModal" class="input-add-btn">
                  <template #icon>
                    <icon-plus />
                  </template>
                </a-button>
                <div v-if="showManufacturerInputs" class="search-dropdown">
                  <div
                    v-for="input in filteredManufacturerInputs"
                    :key="input.value"
                    class="search-option"
                    @click="selectManufacturer(input)"
                  >
                    {{ input.label }}
                  </div>
                  <div v-if="filteredManufacturerInputs.length === 0" class="no-results">Không tìm thấy kết quả phù hợp</div>
                </div>
              </div>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item>
              <template #label>
                <div class="label-with-add-btn">
                  <span class="required-field">Xuất xứ</span>
                </div>
              </template>
              <div class="search-input-container">
                <a-input
                  v-model="originSearchValue"
                  placeholder="Nhập để tìm kiếm xuất xứ"
                  class="search-input"
                  @input="handleOriginInput"
                  @focus="() => handleOriginInput(originSearchValue || '')"
                  @blur="handleOriginBlur"
                />
                <a-button type="text" size="mini" @click="showAddOriginModal" class="input-add-btn">
                  <template #icon>
                    <icon-plus />
                  </template>
                </a-button>
                <div v-if="showOriginInputs" class="search-dropdown">
                  <div v-for="input in filteredOriginInputs" :key="input.value" class="search-option" @click="selectOrigin(input)">
                    {{ input.label }}
                  </div>
                  <div v-if="filteredOriginInputs.length === 0" class="no-results">Không tìm thấy kết quả phù hợp</div>
                </div>
              </div>
            </a-form-item>
          </a-col>
        </a-row>

        <!-- Material and Shoe Sole Row -->
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item>
              <template #label>
                <div class="label-with-add-btn">
                  <span class="required-field">Chất liệu</span>
                </div>
              </template>
              <div class="search-input-container">
                <a-input
                  v-model="materialSearchValue"
                  placeholder="Nhập để tìm kiếm chất liệu"
                  class="search-input"
                  @input="handleMaterialInput"
                  @focus="() => handleMaterialInput(materialSearchValue || '')"
                  @blur="handleMaterialBlur"
                />
                <a-button type="text" size="mini" @click="showAddMaterialModal" class="input-add-btn">
                  <template #icon>
                    <icon-plus />
                  </template>
                </a-button>
                <div v-if="showMaterialInputs" class="search-dropdown">
                  <div v-for="input in filteredMaterialInputs" :key="input.value" class="search-option" @click="selectMaterial(input)">
                    {{ input.label }}
                  </div>
                  <div v-if="filteredMaterialInputs.length === 0" class="no-results">Không tìm thấy kết quả phù hợp</div>
                </div>
              </div>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item>
              <template #label>
                <div class="label-with-add-btn">
                  <span class="required-field">Đế giày</span>
                </div>
              </template>
              <div class="search-input-container">
                <a-input
                  v-model="shoeSoleSearchValue"
                  placeholder="Nhập để tìm kiếm đế giày"
                  class="search-input"
                  @input="handleShoeSoleInput"
                  @focus="() => handleShoeSoleInput(shoeSoleSearchValue || '')"
                  @blur="handleShoeSoleBlur"
                />
                <a-button type="text" size="mini" @click="showAddShoeSoleModal" class="input-add-btn">
                  <template #icon>
                    <icon-plus />
                  </template>
                </a-button>
                <div v-if="showShoeSoleInputs" class="search-dropdown">
                  <div v-for="input in filteredShoeSoleInputs" :key="input.value" class="search-option" @click="selectShoeSole(input)">
                    {{ input.label }}
                  </div>
                  <div v-if="filteredShoeSoleInputs.length === 0" class="no-results">Không tìm thấy kết quả phù hợp</div>
                </div>
              </div>
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>
    </a-card>

    <!-- Product Attributes -->
    <a-card title="Thuộc tính sản phẩm" class="product-form-card" style="margin-top: 16px">
      <a-form :model="formData" :rules="formRules" ref="attributesFormRef" layout="vertical">
        <!-- Colors -->
        <a-row :gutter="16" class="attribute-row">
          <a-col :span="24">
            <a-form-item>
              <template #label>
                <div class="label-with-add-btn">
                  <span class="required-field">Màu sắc</span>
                  <a-button type="text" size="mini" @click="showAddColorModal" class="add-btn">
                    <template #icon>
                      <icon-plus />
                    </template>
                  </a-button>
                </div>
              </template>
              <div class="attribute-selector">
                <a-button type="outline" @click="showColorModal" class="select-attribute-btn">
                  <template #icon>
                    <icon-palette />
                  </template>
                  Chọn màu sắc ({{ formData.colors.length }})
                </a-button>
                <div class="selected-items">
                  <a-tag v-for="color in formData.colors" :key="color" closable @close="removeColor(color)" class="color-tag">
                    {{ colorInputs.find((c) => c.value === color)?.label }}
                  </a-tag>
                </div>
              </div>
            </a-form-item>
          </a-col>
        </a-row>

        <!-- Sizes -->
        <a-row :gutter="16" class="attribute-row">
          <a-col :span="24">
            <a-form-item>
              <template #label>
                <div class="label-with-add-btn">
                  <span class="required-field">Kích thước</span>
                  <a-button type="text" size="mini" @click="showAddSizeModal" class="add-btn">
                    <template #icon>
                      <icon-plus />
                    </template>
                  </a-button>
                </div>
              </template>
              <div class="attribute-selector">
                <a-button type="outline" @click="showSizeModal" class="select-attribute-btn">
                  <template #icon>
                    <icon-expand />
                  </template>
                  Chọn kích thước ({{ formData.sizes.length }})
                </a-button>
                <div class="selected-items">
                  <a-tag v-for="size in formData.sizes" :key="size" closable @close="removeSize(size)" class="size-tag">
                    {{ sizeInputs.find((s) => s.value === size)?.label }}
                  </a-tag>
                </div>
              </div>
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>
    </a-card>

    <!-- Product Variants -->
    <a-card v-if="variants.length > 0" title="Biến thể sản phẩm" class="product-form-card" style="margin-top: 16px">
      <div class="variants-header">
        <a-button type="primary" @click="showQuickAddModal">
          <template #icon>
            <icon-plus />
          </template>
          Thêm nhanh
        </a-button>
      </div>
      <div v-for="colorVariant in variants" :key="colorVariant.color" class="color-variant">
        <h4>{{ colorInputs.find((c) => c.value === colorVariant.color)?.label }}</h4>
        <a-table
          :columns="variantColumns"
          :data="colorVariant.variants"
          :pagination="{
            showTotal: true,
            showSizeChanger: true,
            pageSizeOptions: ['5', '10', '20'],
            defaultPageSize: 10,
            size: 'small',
          }"
          size="small"
          class="variant-table"
        >
          <template #weight="{ record }">
            <a-input-number v-model="record.weight" :min="0" :step="0.1" size="small" style="width: 100px" :precision="1" />
          </template>
          <template #stock="{ record }">
            <a-input-number v-model="record.stock" :min="0" size="small" style="width: 80px" />
          </template>
          <template #price="{ record }">
            <a-input-number
              v-model="record.price"
              :min="0"
              :step="1000"
              size="small"
              style="width: 100px"
              :formatter="(value) => `${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')"
            />
          </template>
          <template #attributes="{ record }">
            <div class="attributes-list">
              <div v-for="attr in record.attributes || []" :key="attr.label" class="attribute-line">
                <span class="attr-label">{{ attr.label }}:</span>
                <span class="attr-value">{{ attr.value }}</span>
              </div>
            </div>
          </template>
          <template #actions="{ record }">
            <a-button type="text" size="small" @click="deleteVariant(record.sku)" danger>
              <template #icon>
                <icon-delete />
              </template>
            </a-button>
          </template>
        </a-table>
      </div>
    </a-card>

    <!-- Image Management Card -->
    <a-card v-if="formData.colors.length > 0" title="Quản lý ảnh sản phẩm theo màu" class="image-management-card" style="margin-top: 16px">
      <div class="color-image-grid">
        <div v-for="color in formData.colors" :key="color" class="color-image-section">
          <div class="color-header">
            <div class="color-info">
              <div class="color-preview" :style="{ backgroundColor: colorInputs.find((c) => c.value === color)?.maMau || '#ccc' }"></div>
              <span class="color-name">{{ colorInputs.find((c) => c.value === color)?.label }}</span>
            </div>
            <a-button type="outline" size="small" @click="showImageModal(color)">
              <template #icon>
                <icon-camera />
              </template>
              Thêm ảnh ({{ getColorImages(color).length }}/5)
            </a-button>
          </div>
          <div class="color-images">
            <div v-if="getColorImages(color).length === 0" class="no-images">
              <icon-folder />
              <span>Chưa có ảnh</span>
            </div>
            <div v-else class="image-thumbnails">
              <div v-for="(image, index) in getColorImages(color)" :key="index" class="image-thumbnail">
                <img :src="image.url" :alt="image.name" />
                <div class="image-overlay">
                  <a-button type="text" size="mini" @click="removeColorImage(color, index)" danger>
                    <template #icon>
                      <icon-delete />
                    </template>
                  </a-button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Action Buttons -->
      <div class="variants-actions" style="margin-top: 24px; text-align: center">
        <a-space>
          <a-button @click="handleCancel">
            <template #icon>
              <icon-close />
            </template>
            Huỷ bỏ
          </a-button>
          <a-button type="primary" @click="handleSubmit" :loading="loading">
            <template #icon>
              <icon-check />
            </template>
            Thêm biến thể
          </a-button>
        </a-space>
      </div>
    </a-card>

    <!-- Quick Add Modal -->
    <a-modal v-model:visible="quickAddVisible" title="Thêm nhanh biến thể" width="600px" @ok="handleQuickAdd" @cancel="resetQuickAddForm">
      <a-form :model="quickAddForm" :rules="quickAddRules" ref="quickAddFormRef" layout="vertical">
        <!-- Color Selection -->
        <a-form-item>
          <template #label>
            <span class="required-field">Chọn màu sắc áp dụng</span>
          </template>
          <a-checkbox-group v-model="quickAddForm.selectedColors" @change="handleColorSelectionChange">
            <a-checkbox
              v-for="color in formData.colors"
              :key="color"
              :value="color"
              :disabled="quickAddForm.selectedColors.includes('all')"
            >
              {{ colorInputs.find((c) => c.value === color)?.label }}
            </a-checkbox>
            <a-checkbox value="all" style="margin-top: 8px; font-weight: 500">Chọn tất cả màu</a-checkbox>
          </a-checkbox-group>
        </a-form-item>

        <!-- Weight -->
        <a-form-item>
          <template #label>
            <span class="required-field">Trọng lượng (kg)</span>
          </template>
          <a-input-number
            v-model="quickAddForm.weight"
            :min="0"
            :step="0.1"
            placeholder="Nhập trọng lượng"
            :precision="1"
            style="width: 100%"
          />
        </a-form-item>

        <!-- Stock -->
        <a-form-item>
          <template #label>
            <span class="required-field">Số lượng</span>
          </template>
          <a-input-number v-model="quickAddForm.stock" :min="0" placeholder="Nhập số lượng" style="width: 100%" />
        </a-form-item>

        <!-- Price -->
        <a-form-item>
          <template #label>
            <span class="required-field">Đơn giá (VNĐ)</span>
          </template>
          <a-input-number
            v-model="quickAddForm.price"
            :min="0"
            :step="1000"
            placeholder="Nhập đơn giá"
            :formatter="(value) => `${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')"
            :parser="(value) => value.replace(/\$\s?|(,*)/g, '')"
            style="width: 100%"
          />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- Color Selection Modal -->
    <a-modal
      v-model:visible="colorModalVisible"
      title="Chọn màu sắc"
      width="500px"
      :mask-closable="true"
      :closable="true"
      @ok="handleColorModalOk"
      @cancel="handleColorModalCancel"
    >
      <div class="attribute-modal-content">
        <!-- Search Input for Colors -->
        <div class="search-section">
          <a-input v-model="colorSearchText" placeholder="Tìm kiếm màu sắc..." allow-clear @clear="colorSearchText = ''">
            <template #prefix>
              <icon-search />
            </template>
          </a-input>
        </div>

        <div class="attribute-inputs-list">
          <div v-for="color in filteredColorInputs" :key="color.value" class="color-input-item">
            <a-checkbox
              :checked="tempColors.includes(color.value)"
              @change="(checked) => handleColorInputChange(color.value, checked)"
              class="attribute-input-checkbox"
            >
              {{ color.label }}
            </a-checkbox>
            <input type="color" :value="color.maMau" disabled class="color-preview-input" />
          </div>
          <div v-if="filteredColorInputs.length === 0" class="no-results">Không tìm thấy màu sắc phù hợp</div>
        </div>
      </div>
    </a-modal>

    <!-- Size Selection Modal -->
    <a-modal
      v-model:visible="sizeModalVisible"
      title="Chọn kích thước"
      width="500px"
      :mask-closable="true"
      :closable="true"
      @ok="handleSizeModalOk"
      @cancel="handleSizeModalCancel"
    >
      <div class="attribute-modal-content">
        <!-- Search Input for Sizes -->
        <div class="search-section">
          <a-input v-model="sizeSearchText" placeholder="Tìm kiếm kích thước..." allow-clear @clear="sizeSearchText = ''">
            <template #prefix>
              <icon-search />
            </template>
          </a-input>
        </div>

        <div class="attribute-inputs-list">
          <a-checkbox
            v-for="size in filteredSizeInputs"
            :key="size.value"
            :checked="tempSizes.includes(size.value)"
            @change="(checked) => handleSizeInputChange(size.value, checked)"
            class="attribute-input-checkbox"
          >
            {{ size.label }}
          </a-checkbox>
          <div v-if="filteredSizeInputs.length === 0" class="no-results">Không tìm thấy kích thước phù hợp</div>
        </div>
      </div>
    </a-modal>
    <!-- Add Manufacturer Modal -->
    <a-modal
      v-model:visible="addManufacturerModalVisible"
      title="Thêm nhà sản xuất"
      width="500px"
      :mask-closable="false"
      :closable="true"
      @ok="handleAddManufacturer"
      @cancel="closeAddManufacturerModal"
    >
      <a-form :model="newManufacturerForm" :rules="newAttributeRules" ref="newManufacturerFormRef" layout="vertical">
        <a-form-item>
          <template #label>
            <span class="required-field">Tên nhà sản xuất</span>
          </template>
          <a-input v-model="newManufacturerForm.tenNhaSanXuat" placeholder="Nhập tên nhà sản xuất" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- Add Origin Modal -->
    <a-modal
      v-model:visible="addOriginModalVisible"
      title="Thêm xuất xứ"
      width="500px"
      :mask-closable="false"
      :closable="true"
      @ok="handleAddOrigin"
      @cancel="closeAddOriginModal"
    >
      <a-form :model="newOriginForm" :rules="newAttributeRules" ref="newOriginFormRef" layout="vertical">
        <a-form-item>
          <template #label>
            <span class="required-field">Tên xuất xứ</span>
          </template>
          <a-input v-model="newOriginForm.tenXuatXu" placeholder="Nhập tên xuất xứ" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- Add Material Modal -->
    <a-modal
      v-model:visible="addMaterialModalVisible"
      title="Thêm chất liệu"
      width="500px"
      :mask-closable="false"
      :closable="true"
      @ok="handleAddMaterial"
      @cancel="closeAddMaterialModal"
    >
      <a-form :model="newMaterialForm" :rules="newAttributeRules" ref="newMaterialFormRef" layout="vertical">
        <a-form-item>
          <template #label>
            <span class="required-field">Tên chất liệu</span>
          </template>
          <a-input v-model="newMaterialForm.tenChatLieu" placeholder="Nhập tên chất liệu" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- Add Shoe Sole Modal -->
    <a-modal
      v-model:visible="addShoeSoleModalVisible"
      title="Thêm đế giày"
      width="500px"
      :mask-closable="false"
      :closable="true"
      @ok="handleAddShoeSole"
      @cancel="closeAddShoeSoleModal"
    >
      <a-form :model="newShoeSoleForm" :rules="newAttributeRules" ref="newShoeSoleFormRef" layout="vertical">
        <a-form-item>
          <template #label>
            <span class="required-field">Tên đế giày</span>
          </template>
          <a-input v-model="newShoeSoleForm.tenDeGiay" placeholder="Nhập tên đế giày" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- Add Color Modal -->
    <a-modal
      v-model:visible="addColorModalVisible"
      title="Thêm màu sắc mới"
      width="400px"
      @ok="handleAddColor"
      @cancel="closeAddColorModal"
    >
      <a-form ref="newColorFormRef" :model="newColorForm" :rules="newColorFormRules" layout="vertical">
        <a-form-item field="maMau" label="Chọn màu">
          <input
            type="color"
            :value="newColorForm.maMau"
            @input="onColorChange($event.target.value)"
            style="width: 100%; height: 40px; border: 1px solid #d9d9d9; border-radius: 6px; cursor: pointer"
          />
        </a-form-item>
        <a-form-item field="tenMauSac" label="Tên màu sắc">
          <a-input v-model="newColorForm.tenMauSac" placeholder="Tên màu sẽ tự động điền" readonly />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- Add Size Modal -->
    <a-modal
      v-model:visible="addSizeModalVisible"
      title="Thêm kích thước mới"
      width="400px"
      @ok="handleAddSize"
      @cancel="closeAddSizeModal"
    >
      <a-form ref="newSizeFormRef" :model="newSizeForm" :rules="newSizeFormRules" layout="vertical">
        <a-form-item field="tenKichThuoc" label="Tên kích thước">
          <a-input v-model="newSizeForm.tenKichThuoc" placeholder="Nhập tên kích thước" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- Image Management Modal -->
    <a-modal
      v-model:visible="imageModalVisible"
      :title="`Quản lý ảnh - ${imageModalColor ? colorInputs.find((c) => c.value === imageModalColor)?.label : ''}`"
      width="700px"
      :ok-loading="uploadingImages"
      :cancel-button-props="{ disabled: uploadingImages }"
      @ok="handleImageModalOk"
      @cancel="handleImageModalCancel"
    >
      <div class="image-modal-content">
        <!-- Existing Images from Database -->
        <div class="existing-images-section">
          <h4>Ảnh có sẵn từ database</h4>
          <div v-if="existingImages.length === 0" class="no-existing-images">
            <a-spin v-if="loadingExistingImages" />
            <span v-else>Không có ảnh nào cho màu này</span>
          </div>
          <div v-else class="existing-images-grid">
            <div
              v-for="(image, index) in existingImages"
              :key="index"
              class="existing-image-item"
              :class="{ selected: selectedExistingImages.includes(image.id) }"
              @click="handleExistingImageSelect(image.id)"
            >
              <img :src="image.duongDanAnh" :alt="image.tenAnh || 'Ảnh sản phẩm'" />
              <div class="image-check-overlay" v-if="selectedExistingImages.includes(image.id)">
                <icon-check />
              </div>
            </div>
          </div>
        </div>

        <!-- Upload New Images -->
        <a-divider />
        <div class="upload-new-section">
          <h4>Upload ảnh mới</h4>
          <a-upload
            v-model:file-list="newUploadImages"
            list-type="picture-card"
            :multiple="true"
            :max="5"
            accept="image/*"
            :before-upload="handleBeforeUpload"
            :custom-request="() => {}"
            :show-upload-list="{ showPreviewIcon: true, showRemoveIcon: true }"
            class="new-image-upload"
          >
            <template #upload-button>
              <div v-if="newUploadImages.length < 5" class="upload-button">
                <icon-plus />
                <div>Upload ảnh</div>
              </div>
            </template>
          </a-upload>
          <div class="upload-note">
            <p>• Hỗ trợ định dạng: JPG, PNG, GIF, WebP</p>
            <p>• Kích thước tối đa: 10MB mỗi file</p>
            <p>• Tối đa 5 ảnh mỗi lần upload</p>
          </div>
        </div>
      </div>
    </a-modal>

    <!-- Upload Loading Overlay -->
    <div v-if="uploadingImages" class="upload-loading-overlay">
      <div class="upload-loading-content">
        <a-spin size="large" />
        <div class="upload-loading-text">
          <h3>Đang upload ảnh...</h3>
          <p>Vui lòng chờ trong giây lát. Quá trình này có thể mất tối đa 30 giây.</p>
          <div class="upload-loading-info">
            <p class="loading-text">{{ uploadProgressText }}</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { Message } from '@arco-design/web-vue'
import Breadcrumb from '@/components/breadcrumb/breadcrumb.vue'
import useBreadcrumb from '@/hooks/breadcrumb'
import {
  IconCheck,
  IconClose,
  IconDelete,
  IconPlus,
  IconPalette,
  IconExpand,
  IconCamera,
  IconFolder,
  IconSearch,
} from '@arco-design/web-vue/es/icon'
import { useUserStore } from '@/store'
// Import API functions
import {
  getSanPhamOptions,
  getMauSacOptions,
  getKichThuocOptions,
  getChatLieuOptions,
  getDeGiayOptions,
  getNhaSanXuatOptions,
  getXuatXuOptions,
  createBienTheSanPham,
  createDanhMucSanPham,
  createNhaSanXuat,
  createXuatXu,
  createChatLieu,
  createDeGiay,
  createMauSac,
  createKichThuoc,
  themAnhChoBienThe,
} from '@/api/san-pham'
import { getAnhSanPhamByMauAnh } from '@/api/san-pham/bien-the'
import { uploadMutipartFile, getAnhSanPhamByTenMau } from '@/api/san-pham/thuoc-tinh/anh-san-pham'

// Breadcrumb setup
const { breadcrumbItems } = useBreadcrumb()
const router = useRouter()
const userStore = useUserStore()

// Form state
const loading = ref(false)
const formRef = ref()
const attributesFormRef = ref()

// Quick add modal state
const quickAddVisible = ref(false)
const quickAddFormRef = ref()

// Attribute modal states
const colorModalVisible = ref(false)
const sizeModalVisible = ref(false)
const tempColors = ref<string[]>([])
const tempSizes = ref<string[]>([])

// Add modals state
const addProductModalVisible = ref(false)
const addManufacturerModalVisible = ref(false)
const addOriginModalVisible = ref(false)
const addMaterialModalVisible = ref(false)
const addShoeSoleModalVisible = ref(false)
const addColorModalVisible = ref(false)
const addSizeModalVisible = ref(false)

// Search states for modal filters
const colorSearchText = ref('')
const sizeSearchText = ref('')

// Image management state
const imageModalVisible = ref(false)
const imageModalColor = ref<string | null>(null)
const colorImages = ref<Record<string, any[]>>({})
const existingImages = ref<any[]>([])
const loadingExistingImages = ref(false)
const selectedExistingImages = ref<string[]>([])
const newUploadImages = ref<any[]>([])
const uploadingImages = ref(false)
const uploadProgress = ref(0)
const uploadProgressText = ref('')

// Storage for uploaded image IDs per color - lưu IDs của ảnh đã upload
const uploadedImageStore = ref<Record<string, number[]>>({})
const uploadingFiles = ref<Set<string>>(new Set()) // Track uploading files

// Form refs for add modals
const newManufacturerFormRef = ref()
const newOriginFormRef = ref()
const newMaterialFormRef = ref()
const newShoeSoleFormRef = ref()
const newColorFormRef = ref()
const newSizeFormRef = ref()

const newManufacturerForm = reactive({
  tenNhaSanXuat: '',
})

const newOriginForm = reactive({
  tenXuatXu: '',
})

const newMaterialForm = reactive({
  tenChatLieu: '',
})

const newShoeSoleForm = reactive({
  tenDeGiay: '',
})

const newColorForm = reactive({
  tenMauSac: '',
  maMau: '#000000',
})

const newSizeForm = reactive({
  tenKichThuoc: '',
})

// Autocomplete state for product name
const productNameInputs = ref<any[]>([])
const showProductNameInputs = ref(false)
const filteredProductInputs = ref<any[]>([])

// Search states for all inputs
const showManufacturerInputs = ref(false)
const showOriginInputs = ref(false)
const showMaterialInputs = ref(false)
const showShoeSoleInputs = ref(false)
const filteredManufacturerInputs = ref<any[]>([])
const filteredOriginInputs = ref<any[]>([])
const filteredMaterialInputs = ref<any[]>([])
const filteredShoeSoleInputs = ref<any[]>([])

// Input values for search
const manufacturerSearchValue = ref('')
const originSearchValue = ref('')
const materialSearchValue = ref('')
const shoeSoleSearchValue = ref('')

// Pending product to be created on submit
const pendingNewProduct = ref<any>(null)

// Form data
const formData = reactive({
  name: '',
  manufacturer: '',
  origin: '',
  material: '',
  shoeSole: '',
  // Product attributes
  colors: [] as string[],
  sizes: [] as string[],
  // Selected product info for variant creation
  selectedProductId: null as number | null,
})

// Quick add form
const quickAddForm = reactive({
  selectedColors: [] as string[],
  weight: 0,
  stock: 0,
  price: 0,
})

// Dynamic inputs loaded from API
const manufacturerInputs = ref<{ label: string; value: any }[]>([])
const originInputs = ref<{ label: string; value: any }[]>([])
const colorInputs = ref<{ label: string; value: any }[]>([])
const sizeInputs = ref<{ label: string; value: any }[]>([])
const materialInputs = ref<{ label: string; value: any }[]>([])
const shoeSoleInputs = ref<{ label: string; value: any }[]>([])

// Load all data from APIs
const loadAllInputs = async () => {
  try {
    // Load product names for autocomplete
    const productResponse = await getSanPhamOptions()
    if (productResponse.data) {
      productNameInputs.value = productResponse.data.map((item: any) => ({
        label: item.tenSanPham,
        value: item.tenSanPham,
        id: item.id,
        nhaSanXuat: item.nhaSanXuat,
        xuatXu: item.xuatXu,
      }))
    }

    // Load manufacturers
    const manufacturerResponse = await getNhaSanXuatOptions()
    if (manufacturerResponse.data && Array.isArray(manufacturerResponse.data)) {
      manufacturerInputs.value = manufacturerResponse.data.map((item: any) => ({
        label: item.tenNhaSanXuat,
        value: item.id,
      }))
    }

    // Load origins
    const originResponse = await getXuatXuOptions()
    if (originResponse.data && Array.isArray(originResponse.data)) {
      originInputs.value = originResponse.data.map((item: any) => ({
        label: item.tenXuatXu,
        value: item.id,
      }))
    }

    // Load colors
    const colorResponse = await getMauSacOptions()
    if (colorResponse.data) {
      colorInputs.value = colorResponse.data.map((item: any) => ({
        label: item.tenMauSac,
        value: item.id,
        maMau: item.maMau,
      }))
    }

    // Load sizes
    const sizeResponse = await getKichThuocOptions()
    if (sizeResponse.data) {
      sizeInputs.value = sizeResponse.data.map((item: any) => ({
        label: item.tenKichThuoc,
        value: item.id,
      }))
    }

    // Load materials
    const materialResponse = await getChatLieuOptions()
    if (materialResponse.data) {
      materialInputs.value = materialResponse.data.map((item: any) => ({
        label: item.tenChatLieu,
        value: item.id,
      }))
    }

    // Load shoe soles
    const shoeSoleResponse = await getDeGiayOptions()
    if (shoeSoleResponse.data) {
      shoeSoleInputs.value = shoeSoleResponse.data.map((item: any) => ({
        label: item.tenDeGiay,
        value: item.id,
      }))
    }
  } catch (error) {
    Message.error(`Lỗi khi tải dữ liệu: ${error}`)
  }
}

// Individual load functions for refreshing after adding new items
const loadManufacturerInputs = async () => {
  try {
    const response = await getNhaSanXuatOptions()
    if (response.data && Array.isArray(response.data)) {
      manufacturerInputs.value = response.data.map((item: any) => ({
        label: item.tenNhaSanXuat,
        value: item.id,
      }))
    }
  } catch (error) {
    console.error('Error loading manufacturer inputs:', error)
  }
}

const loadOriginInputs = async () => {
  try {
    const response = await getXuatXuOptions()
    if (response.data && Array.isArray(response.data)) {
      originInputs.value = response.data.map((item: any) => ({
        label: item.tenXuatXu,
        value: item.id,
      }))
    }
  } catch (error) {
    console.error('Error loading origin inputs:', error)
  }
}

const loadMaterialInputs = async () => {
  try {
    const response = await getChatLieuOptions()
    if (response.data) {
      materialInputs.value = response.data.map((item: any) => ({
        label: item.tenChatLieu,
        value: item.id,
      }))
    }
  } catch (error) {
    console.error('Error loading material inputs:', error)
  }
}

const loadShoeSoleInputs = async () => {
  try {
    const response = await getDeGiayOptions()
    if (response.data) {
      shoeSoleInputs.value = response.data.map((item: any) => ({
        label: item.tenDeGiay,
        value: item.id,
      }))
    }
  } catch (error) {
    console.error('Error loading shoe sole inputs:', error)
  }
}

const loadColorInputs = async () => {
  try {
    const response = await getMauSacOptions()
    if (response.data) {
      colorInputs.value = response.data.map((item: any) => ({
        label: item.tenMauSac,
        value: item.id,
        maMau: item.maMau,
      }))
    }
  } catch (error) {
    console.error('Error loading color inputs:', error)
  }
}

const loadSizeInputs = async () => {
  try {
    const response = await getKichThuocOptions()
    if (response.data) {
      sizeInputs.value = response.data
        .map((item: any) => ({
          label: item.tenKichThuoc,
          value: item.id,
        }))
        .sort((a, b) => {
          // Sắp xếp kích thước theo thứ tự giảm dần (từ lớn đến nhỏ)
          const parseSize = (size: string) => {
            const num = parseFloat(size)
            return Number.isNaN(num) ? 0 : num
          }
          return parseSize(b.label) - parseSize(a.label)
        })
    }
  } catch (error) {
    console.error('Error loading size inputs:', error)
  }
}

// Product name autocomplete functions
const handleProductNameInput = (value: string) => {
  // Reset pending new product when user changes input
  if (formData.selectedProductId === 'pending') {
    formData.selectedProductId = ''
    pendingNewProduct.value = null
  }

  if (value.length > 0) {
    filteredProductInputs.value = productNameInputs.value.filter((input) => input.label.toLowerCase().includes(value.toLowerCase()))
    showProductNameInputs.value = filteredProductInputs.value.length > 0
  } else {
    // Show all inputs when input is empty
    filteredProductInputs.value = productNameInputs.value
    showProductNameInputs.value = productNameInputs.value.length > 0
  }
}

const selectProductName = (input: any) => {
  formData.name = input.label
  formData.selectedProductId = input.id
  showProductNameInputs.value = false

  // Reset pending new product since user selected existing product
  pendingNewProduct.value = null

  // Auto-fill manufacturer and origin if available
  if (input.nhaSanXuat) {
    formData.manufacturer = input.nhaSanXuat.id
    manufacturerSearchValue.value = input.nhaSanXuat.tenNhaSanXuat
  }
  if (input.xuatXu) {
    formData.origin = input.xuatXu.id
    originSearchValue.value = input.xuatXu.tenXuatXu
  }
}

const handleProductNameBlur = () => {
  // Use setTimeout to allow click events on inputs to fire first
  setTimeout(() => {
    showProductNameInputs.value = false

    // Check if product name doesn't match any existing product
    if (formData.name && formData.name.trim()) {
      const existingProduct = productNameInputs.value.find((input) => input.label.toLowerCase() === formData.name.toLowerCase())

      if (!existingProduct) {
        // Create new product automatically
        formData.selectedProductId = 'pending'
        pendingNewProduct.value = {
          tenSanPham: formData.name.trim(),
          // Will use selected manufacturer and origin when creating
        }
      }
    }
  }, 200)
}

// Search functions for other inputs
const handleManufacturerInput = (value: string) => {
  manufacturerSearchValue.value = value
  if (value.length > 0) {
    filteredManufacturerInputs.value = manufacturerInputs.value.filter((input) => input.label.toLowerCase().includes(value.toLowerCase()))
    showManufacturerInputs.value = filteredManufacturerInputs.value.length > 0
  } else {
    filteredManufacturerInputs.value = manufacturerInputs.value
    showManufacturerInputs.value = manufacturerInputs.value.length > 0
  }
}

const selectManufacturer = (input: any) => {
  formData.manufacturer = input.value
  manufacturerSearchValue.value = input.label
  showManufacturerInputs.value = false
}

const handleManufacturerBlur = () => {
  setTimeout(() => {
    showManufacturerInputs.value = false
  }, 200)
}

const handleOriginInput = (value: string) => {
  originSearchValue.value = value
  if (value.length > 0) {
    filteredOriginInputs.value = originInputs.value.filter((input) => input.label.toLowerCase().includes(value.toLowerCase()))
    showOriginInputs.value = filteredOriginInputs.value.length > 0
  } else {
    filteredOriginInputs.value = originInputs.value
    showOriginInputs.value = originInputs.value.length > 0
  }
}

const selectOrigin = (input: any) => {
  formData.origin = input.value
  originSearchValue.value = input.label
  showOriginInputs.value = false
}

const handleOriginBlur = () => {
  setTimeout(() => {
    showOriginInputs.value = false
  }, 200)
}

const handleMaterialInput = (value: string) => {
  materialSearchValue.value = value
  if (value.length > 0) {
    filteredMaterialInputs.value = materialInputs.value.filter((input) => input.label.toLowerCase().includes(value.toLowerCase()))
    showMaterialInputs.value = filteredMaterialInputs.value.length > 0
  } else {
    filteredMaterialInputs.value = materialInputs.value
    showMaterialInputs.value = materialInputs.value.length > 0
  }
}

const selectMaterial = (input: any) => {
  formData.material = input.value
  materialSearchValue.value = input.label
  showMaterialInputs.value = false
}

const handleMaterialBlur = () => {
  setTimeout(() => {
    showMaterialInputs.value = false
  }, 200)
}

const handleShoeSoleInput = (value: string) => {
  shoeSoleSearchValue.value = value
  if (value.length > 0) {
    filteredShoeSoleInputs.value = shoeSoleInputs.value.filter((input) => input.label.toLowerCase().includes(value.toLowerCase()))
    showShoeSoleInputs.value = filteredShoeSoleInputs.value.length > 0
  } else {
    filteredShoeSoleInputs.value = shoeSoleInputs.value
    showShoeSoleInputs.value = shoeSoleInputs.value.length > 0
  }
}

const selectShoeSole = (input: any) => {
  formData.shoeSole = input.value
  shoeSoleSearchValue.value = input.label
  showShoeSoleInputs.value = false
}

const handleShoeSoleBlur = () => {
  setTimeout(() => {
    showShoeSoleInputs.value = false
  }, 200)
}

// Computed properties for filtered inputs in modals
const filteredColorInputs = computed(() => {
  if (!colorSearchText.value) return colorInputs.value
  return colorInputs.value.filter((color) => color.label.toLowerCase().includes(colorSearchText.value.toLowerCase()))
})

const filteredSizeInputs = computed(() => {
  let filtered = sizeInputs.value

  if (sizeSearchText.value) {
    filtered = sizeInputs.value.filter((size) => size.label.toLowerCase().includes(sizeSearchText.value.toLowerCase()))
  }

  // Sắp xếp kích thước theo thứ tự giảm dần (từ lớn đến nhỏ)
  return filtered.sort((a, b) => {
    const parseSize = (size: string) => {
      const num = parseFloat(size)
      return Number.isNaN(num) ? 0 : num
    }
    return parseSize(b.label) - parseSize(a.label)
  })
})

// Computed: Generate base variants based on selected colors and sizes
const baseVariants = computed(() => {
  if (!formData.colors.length || !formData.sizes.length) return []

  return formData.colors.map((color) => {
    const colorVariants = formData.sizes.map((size) => ({
      // Basic info
      sku: `${formData.name.replace(/\s+/g, '-').toLowerCase()}-${color}-${size}`,
      productName: formData.name,
      manufacturer: manufacturerInputs.value.find((m) => m.value === formData.manufacturer)?.label || 'Chưa chọn',
      origin: originInputs.value.find((o) => o.value === formData.origin)?.label || 'Chưa chọn',
      attributes: [
        { label: 'Màu sắc', value: colorInputs.value.find((c) => c.value === color)?.label || 'N/A' },
        { label: 'Kích thước', value: sizeInputs.value.find((s) => s.value === size)?.label || 'N/A' },
        { label: 'Chất liệu', value: materialInputs.value.find((m) => m.value === formData.material)?.label || 'Chưa chọn' },
        { label: 'Đế giày', value: shoeSoleInputs.value.find((s) => s.value === formData.shoeSole)?.label || 'Chưa chọn' },
      ],
      weight: 0,
      stock: 0,
      price: 0,
      images: [] as any[],
      // For internal use
      color,
      size,
    }))

    return {
      color,
      variants: colorVariants,
    }
  })
})

// Editable variants ref that can be modified
const variants = ref([])

// Sync base variants to editable variants when base variants change
watch(
  baseVariants,
  (newBaseVariants) => {
    variants.value = JSON.parse(JSON.stringify(newBaseVariants)) // Deep clone to avoid reference issues
  },
  { immediate: true }
)

// Delete variant by SKU
const deleteVariant = (sku: string) => {
  // Find which color group this variant belongs to
  const updatedVariants = variants.value.map((colorGroup) => {
    const variantIndex = colorGroup.variants.findIndex((v) => v.sku === sku)
    if (variantIndex > -1) {
      // Create new array without the deleted item to trigger reactivity
      return {
        ...colorGroup,
        variants: colorGroup.variants.filter((v) => v.sku !== sku),
      }
    }
    return colorGroup
  })

  // Update variants to trigger reactivity
  variants.value = updatedVariants
}

// Variant table columns
const variantColumns = [
  {
    title: 'Tên sản phẩm',
    dataIndex: 'productName',
    width: 150,
  },
  {
    title: 'Thuộc tính',
    dataIndex: 'attributes',
    width: 180,
    slotName: 'attributes',
  },
  {
    title: 'Trọng lượng (kg)',
    dataIndex: 'weight',
    width: 120,
    slotName: 'weight',
  },
  {
    title: 'Số lượng',
    dataIndex: 'stock',
    width: 100,
    slotName: 'stock',
  },
  {
    title: 'Đơn giá (VNĐ)',
    dataIndex: 'price',
    width: 200,
    slotName: 'price',
  },
  {
    title: 'Thao tác',
    dataIndex: 'actions',
    width: 80,
    slotName: 'actions',
  },
]

// Quick add form validation rules
const quickAddRules = {
  selectedColors: [
    { required: true, message: 'Vui lòng chọn ít nhất một màu sắc' },
    { type: 'array', min: 1, message: 'Vui lòng chọn ít nhất một màu sắc' },
  ],
  stock: [
    { required: true, message: 'Vui lòng nhập số lượng' },
    { type: 'number', min: 0, message: 'Số lượng phải lớn hơn hoặc bằng 0' },
  ],
  price: [
    { required: true, message: 'Vui lòng nhập đơn giá' },
    { type: 'number', min: 0, message: 'Đơn giá phải lớn hơn 0' },
  ],
}

// Form validation rules
const formRules = {
  name: [
    { required: true, message: 'Vui lòng nhập tên sản phẩm' },
    { min: 2, max: 100, message: 'Tên sản phẩm phải từ 2-100 ký tự' },
  ],
}

// Rules for new items
const newProductRules = {
  tenSanPham: [
    { required: true, message: 'Vui lòng nhập tên sản phẩm' },
    { min: 2, max: 100, message: 'Tên sản phẩm phải từ 2-100 ký tự' },
  ],
  moTa: [
    { required: true, message: 'Vui lòng nhập mô tả sản phẩm' },
    { min: 10, max: 500, message: 'Mô tả phải từ 10-500 ký tự' },
  ],
}

const newAttributeRules = {
  tenNhaSanXuat: [
    { required: true, message: 'Vui lòng nhập tên nhà sản xuất' },
    { min: 2, max: 50, message: 'Tên nhà sản xuất phải từ 2-50 ký tự' },
  ],
  tenXuatXu: [
    { required: true, message: 'Vui lòng nhập tên xuất xứ' },
    { min: 2, max: 50, message: 'Tên xuất xứ phải từ 2-50 ký tự' },
  ],
  tenChatLieu: [
    { required: true, message: 'Vui lòng nhập tên chất liệu' },
    { min: 2, max: 50, message: 'Tên chất liệu phải từ 2-50 ký tự' },
  ],
  tenDeGiay: [
    { required: true, message: 'Vui lòng nhập tên đế giày' },
    { min: 2, max: 50, message: 'Tên đế giày phải từ 2-50 ký tự' },
  ],
}

const newColorFormRules = {
  maMau: [{ required: true, message: 'Vui lòng chọn màu sắc' }],
}

const newSizeFormRules = {
  tenKichThuoc: [
    { required: true, message: 'Vui lòng nhập tên kích thước' },
    { min: 1, max: 10, message: 'Tên kích thước phải từ 1-10 ký tự' },
  ],
}

// Helper function to get hex color value for color preview
const getColorHexValue = (colorValue: string) => {
  const colorMap: { [key: string]: string } = {
    red: '#FF0000',
    blue: '#0000FF',
    green: '#00FF00',
    yellow: '#FFFF00',
    black: '#000000',
    white: '#FFFFFF',
    gray: '#808080',
    purple: '#800080',
  }
  return colorMap[colorValue] || '#000000'
}

// Color modal methods
const showColorModal = () => {
  tempColors.value = [...formData.colors]
  colorModalVisible.value = true
}

const handleColorModalOk = () => {
  formData.colors = [...tempColors.value]
  colorModalVisible.value = false
}

const handleColorModalCancel = () => {
  // Reset temp colors to original values
  tempColors.value = [...formData.colors]
  colorModalVisible.value = false
}

// Size modal methods
const showSizeModal = () => {
  tempSizes.value = [...formData.sizes]
  sizeModalVisible.value = true
}

const handleSizeModalOk = () => {
  formData.sizes = [...tempSizes.value]
  sizeModalVisible.value = false
}

const handleSizeModalCancel = () => {
  // Reset temp sizes to original values
  tempSizes.value = [...formData.sizes]
  sizeModalVisible.value = false
}

// Individual input change handlers
const handleColorInputChange = (colorValue: string, checked: boolean) => {
  if (checked) {
    if (!tempColors.value.includes(colorValue)) {
      tempColors.value.push(colorValue)
    }
  } else {
    const index = tempColors.value.indexOf(colorValue)
    if (index > -1) {
      tempColors.value.splice(index, 1)
    }
  }
}

const handleSizeInputChange = (sizeValue: string, checked: boolean) => {
  if (checked) {
    if (!tempSizes.value.includes(sizeValue)) {
      tempSizes.value.push(sizeValue)
    }
  } else {
    const index = tempSizes.value.indexOf(sizeValue)
    if (index > -1) {
      tempSizes.value.splice(index, 1)
    }
  }
}

// Remove methods for tags
const removeColor = (color: string) => {
  const index = formData.colors.indexOf(color)
  if (index > -1) {
    formData.colors.splice(index, 1)
  }
}

const removeSize = (size: string) => {
  const index = formData.sizes.indexOf(size)
  if (index > -1) {
    formData.sizes.splice(index, 1)
  }
}

// Quick add modal methods
const resetQuickAddForm = () => {
  quickAddForm.selectedColors = []
  quickAddForm.weight = 0
  quickAddForm.stock = 0
  quickAddForm.price = 0
}

const showQuickAddModal = () => {
  quickAddVisible.value = true
  // Reset form
  resetQuickAddForm()
}

// Handle color selection changes
const handleColorSelectionChange = (selectedValues: string[]) => {
  const hasAll = selectedValues.includes('all')
  const otherColors = selectedValues.filter((v) => v !== 'all')

  if (hasAll && otherColors.length > 0) {
    // If "all" is selected along with other colors, remove "all"
    quickAddForm.selectedColors = otherColors
  } else if (hasAll && otherColors.length === 0) {
    // If only "all" is selected, keep it
    quickAddForm.selectedColors = ['all']
  } else {
    // Normal selection
    quickAddForm.selectedColors = otherColors
  }
}

const handleQuickAdd = async () => {
  try {
    await quickAddFormRef.value.validate()

    // Determine which colors to apply to
    let targetColors = quickAddForm.selectedColors
    if (targetColors.includes('all')) {
      targetColors = formData.colors
    }

    // Apply values to variants of selected colors
    variants.value.forEach((colorGroup) => {
      if (targetColors.includes(colorGroup.color)) {
        colorGroup.variants.forEach((variant) => {
          if (quickAddForm.weight > 0) {
            variant.weight = quickAddForm.weight
          }
          variant.stock = quickAddForm.stock
          variant.price = quickAddForm.price
        })
      }
    })

    quickAddVisible.value = false
    resetQuickAddForm()
  } catch {
    // Handle validation error
  }
}

// Quick add modal methods
const showAddManufacturerModal = () => {
  newManufacturerForm.tenNhaSanXuat = ''
  addManufacturerModalVisible.value = true
}

const closeAddManufacturerModal = () => {
  addManufacturerModalVisible.value = false
  newManufacturerFormRef.value?.resetFields()
}

const handleAddManufacturer = async () => {
  try {
    await newManufacturerFormRef.value.validate()
    const newManufacturer = {
      tenNhaSanXuat: newManufacturerForm.tenNhaSanXuat,
      trangThai: true,
      deleted: false,
      createAt: new Date().toISOString().split('T')[0],
      createBy: userStore.id,
    }

    const response = await createNhaSanXuat(newManufacturer)
    if (response.data) {
      // Reload inputs from API to get latest data
      await loadManufacturerInputs()
      // Force Vue reactivity update and wait for DOM update
      await nextTick()

      // Additional timeout to ensure inputs are rendered
      setTimeout(() => {
        // Select the newly created item
        formData.manufacturer = response.data
        manufacturerSearchValue.value = response.data.tenNhaSanXuat
      }, 100)

      closeAddManufacturerModal()
      Message.success('Nhà sản xuất đã được thêm thành công!')
    }
  } catch (error) {
    Message.error(`Lỗi khi thêm nhà sản xuất: ${error.message || error}`)
  }
}

const showAddOriginModal = () => {
  newOriginForm.tenXuatXu = ''
  addOriginModalVisible.value = true
}

const closeAddOriginModal = () => {
  addOriginModalVisible.value = false
  newOriginFormRef.value?.resetFields()
}

const handleAddOrigin = async () => {
  try {
    await newOriginFormRef.value.validate()

    const newOrigin = {
      tenXuatXu: newOriginForm.tenXuatXu,
      trangThai: true,
      deleted: false,
      createAt: new Date().toISOString().split('T')[0],
      createBy: userStore.id,
    }

    const response = await createXuatXu(newOrigin)
    if (response.data) {
      // Reload inputs from API to get latest data
      await loadOriginInputs()

      // Force Vue reactivity update and wait for DOM update
      await nextTick()

      // Additional timeout to ensure inputs are rendered
      setTimeout(() => {
        // Select the newly created item
        formData.origin = response.data
        originSearchValue.value = response.data.tenXuatXu
      }, 100)

      closeAddOriginModal()
      Message.success('Xuất xứ đã được thêm thành công!')
    }
  } catch (error) {
    Message.error(`Lỗi khi thêm xuất xứ: ${error.message || error}`)
  }
}

const showAddMaterialModal = () => {
  newMaterialForm.tenChatLieu = ''
  addMaterialModalVisible.value = true
}

const closeAddMaterialModal = () => {
  addMaterialModalVisible.value = false
  newMaterialFormRef.value?.resetFields()
}

const handleAddMaterial = async () => {
  try {
    await newMaterialFormRef.value.validate()

    const newMaterial = {
      tenChatLieu: newMaterialForm.tenChatLieu,
      trangThai: true,
      deleted: false,
      createAt: new Date().toISOString().split('T')[0],
      createBy: userStore.id,
    }

    const response = await createChatLieu(newMaterial)

    if (response.data) {
      // Reload inputs from API to get latest data
      await loadMaterialInputs()

      // Force Vue reactivity update and wait for DOM update
      await nextTick()

      // Additional timeout to ensure inputs are rendered
      setTimeout(() => {
        // Select the newly created item
        formData.material = response.data
        materialSearchValue.value = response.data.tenChatLieu
      }, 100)

      closeAddMaterialModal()
      Message.success('Chất liệu đã được thêm thành công!')
    }
  } catch (error) {
    Message.error(`Lỗi khi thêm chất liệu: ${error.message || error}`)
  }
}

const showAddShoeSoleModal = () => {
  newShoeSoleForm.tenDeGiay = ''
  addShoeSoleModalVisible.value = true
}

const closeAddShoeSoleModal = () => {
  addShoeSoleModalVisible.value = false
  newShoeSoleFormRef.value?.resetFields()
}

const handleAddShoeSole = async () => {
  try {
    await newShoeSoleFormRef.value.validate()

    const newShoeSole = {
      tenDeGiay: newShoeSoleForm.tenDeGiay,
      trangThai: true,
      deleted: false,
      createAt: new Date().toISOString().split('T')[0],
      createBy: userStore.id,
    }

    const response = await createDeGiay(newShoeSole)

    if (response.data) {
      // Reload inputs from API to get latest data
      await loadShoeSoleInputs()

      // Force Vue reactivity update and wait for DOM update
      await nextTick()

      // Additional timeout to ensure inputs are rendered
      setTimeout(() => {
        // Select the newly created item
        formData.shoeSole = response.data
        shoeSoleSearchValue.value = response.data.tenDeGiay
      }, 100)

      closeAddShoeSoleModal()
      Message.success('Đế giày đã được thêm thành công!')
    }
  } catch (error) {
    Message.error(`Lỗi khi thêm đế giày: ${error.message || error}`)
  }
}

// Hàm để lấy tên màu từ mã màu
const getColorName = (hexColor) => {
  const color = hexColor.toLowerCase()

  // Mapping tên màu tiếng Việt
  const colorMap = {
    '#000000': 'Đen',
    '#ffffff': 'Trắng',
    '#ff0000': 'Đỏ',
    '#00ff00': 'Xanh lá',
    '#0000ff': 'Xanh dương',
    '#ffff00': 'Vàng',
    '#ff00ff': 'Tím',
    '#00ffff': 'Xanh ngọc',
    '#ffa500': 'Cam',
    '#800080': 'Tím đậm',
    '#008000': 'Xanh lá đậm',
    '#000080': 'Xanh navy',
    '#800000': 'Nâu đỏ',
    '#808080': 'Xám',
    '#c0c0c0': 'Bạc',
    '#008080': 'Xanh teal',
    '#808000': 'Ô liu',
    '#ffc0cb': 'Hồng',
    '#a52a2a': 'Nâu',
    '#dda0dd': 'Tím nhạt',
    '#add8e6': 'Xanh nhạt',
    '#f0f8ff': 'Xanh nhạt',
    '#faebd7': 'Trắng cổ',
    '#7fffd4': 'Xanh ngọc biển',
    '#f0ffff': 'Xanh da trời',
    '#f5f5dc': 'Vàng nhạt',
    '#ffebcd': 'Hạnh nhân nhạt',
    '#8a2be2': 'Tím xanh',
    '#deb887': 'Nâu gỗ',
    '#5f9ea0': 'Xanh cadet',
    '#7fff00': 'Vàng chanh',
    '#d2691e': 'Sô-cô-la',
    '#6495ed': 'Xanh hoa cỏ',
    '#fff8dc': 'Lụa ngô',
    '#00008b': 'Xanh dương đậm',
    '#008b8b': 'Xanh ngọc đậm',
    '#b8860b': 'Vàng kim đậm',
    '#a9a9a9': 'Xám đậm',
    '#006400': 'Xanh lá đậm',
    '#bdb76b': 'Kaki đậm',
    '#8b008b': 'Đỏ tươi đậm',
    '#556b2f': 'Ô liu xanh đậm',
    '#ff8c00': 'Cam đậm',
    '#9932cc': 'Lan đậm',
    '#8b0000': 'Đỏ đậm',
    '#e9967a': 'Hồng cá đậm',
    '#8fbc8f': 'Xanh biển đậm',
    '#483d8b': 'Xanh đá đậm',
    '#2f4f4f': 'Xám đá đậm',
    '#00ced1': 'Thủy ngọc đậm',
    '#9400d3': 'Tía đậm',
    '#ff1493': 'Hồng sâu',
    '#00bfff': 'Xanh trời sâu',
    '#696969': 'Xám mờ',
    '#1e90ff': 'Xanh dodger',
    '#b22222': 'Gạch lửa',
    '#fffaf0': 'Trắng hoa',
    '#dcdcdc': 'Xám gainsboro',
    '#f8f8ff': 'Trắng ma',
    '#daa520': 'Vàng kim',
    '#adff2f': 'Vàng xanh lá',
    '#f0fff0': 'Mật ong',
    '#ff69b4': 'Hồng nóng',
    '#cd5c5c': 'Đỏ ấn độ',
    '#4b0082': 'Chàm',
    '#fffff0': 'Ngà',
    '#f0e68c': 'Kaki',
    '#e6e6fa': 'Oải hương',
    '#fff0f5': 'Hồng oải hương',
    '#7cfc00': 'Xanh cỏ',
    '#fffacd': 'Vàng chanh nhạt',
    '#f08080': 'San hô nhạt',
    '#e0ffff': 'Xanh ngọc nhạt',
    '#fafad2': 'Vàng kim nhạt',
    '#d3d3d3': 'Xám nhạt',
    '#ffb6c1': 'Hồng nhạt',
    '#ffa07a': 'Hồng cá nhạt',
    '#20b2aa': 'Xanh biển nhạt',
    '#87cefa': 'Xanh trời nhạt',
    '#778899': 'Xám đá nhạt',
    '#b0c4de': 'Xanh thép nhạt',
    '#ffffe0': 'Vàng nhạt',
    '#32cd32': 'Chanh',
    '#faf0e6': 'Vải lanh',
    '#66cdaa': 'Xanh ngọc trung bình',
    '#0000cd': 'Xanh dương trung bình',
    '#ba55d3': 'Lan trung bình',
    '#9370db': 'Tím trung bình',
    '#3cb371': 'Xanh biển trung bình',
    '#7b68ee': 'Xanh đá trung bình',
    '#00fa9a': 'Xanh xuân trung bình',
    '#48d1cc': 'Thủy ngọc trung bình',
    '#c71585': 'Đỏ tía trung bình',
    '#191970': 'Xanh nửa đêm',
    '#f5fffa': 'Kem bạc hà',
    '#ffe4e1': 'Hoa hồng sương mù',
    '#ffe4b5': 'Da hươu',
    '#ffdead': 'Trắng navajo',
    '#fdf5e6': 'Ren cũ',
    '#6b8e23': 'Ô liu xám',
    '#ff4500': 'Cam đỏ',
    '#da70d6': 'Lan',
    '#eee8aa': 'Vàng kim nhạt',
    '#98fb98': 'Xanh lá nhạt',
    '#afeeee': 'Thủy ngọc nhạt',
    '#db7093': 'Đỏ tía nhạt',
    '#ffefd5': 'Kem đu đủ',
    '#ffdab9': 'Hồng đào',
    '#cd853f': 'Nâu peru',
    '#b0e0e6': 'Xanh bột',
    '#bc8f8f': 'Nâu hồng',
    '#4169e1': 'Xanh hoàng gia',
    '#8b4513': 'Nâu yên ngựa',
    '#fa8072': 'Hồng cá',
    '#f4a460': 'Nâu cát',
    '#2e8b57': 'Xanh biển',
    '#fff5ee': 'Vỏ sò',
    '#a0522d': 'Nâu sienna',
    '#87ceeb': 'Xanh trời',
    '#6a5acd': 'Xanh đá',
    '#708090': 'Xám đá',
    '#fffafa': 'Tuyết',
    '#00ff7f': 'Xanh xuân',
    '#4682b4': 'Xanh thép',
    '#d2b48c': 'Nâu tan',
    '#d8bfd8': 'Cây kế',
    '#ff6347': 'Cà chua',
    '#40e0d0': 'Thủy ngọc',
    '#ee82ee': 'Tía',
    '#f5deb3': 'Lúa mì',
    '#f5f5f5': 'Khói trắng',
    '#9acd32': 'Vàng xanh lá',
  }

  // Kiểm tra màu chính xác
  if (colorMap[color]) {
    return colorMap[color]
  }

  // Phân tích màu theo RGB
  const r = parseInt(color.slice(1, 3), 16)
  const g = parseInt(color.slice(3, 5), 16)
  const b = parseInt(color.slice(5, 7), 16)

  // Xác định màu dominant
  const max = Math.max(r, g, b)
  const min = Math.min(r, g, b)
  const diff = max - min

  if (diff < 30) {
    // Màu xám
    if (max < 85) return 'Đen'
    if (max > 170) return 'Trắng'
    return 'Xám'
  }

  if (r === max) {
    if (g > b) return 'Vàng'
    return 'Đỏ'
  }
  if (g === max) {
    if (r > b) return 'Vàng'
    return 'Xanh lá'
  }
  if (r > g) return 'Tím'
  return 'Xanh dương'
}

// Hàm xử lý khi thay đổi màu
const onColorChange = (value) => {
  newColorForm.maMau = value
  newColorForm.tenMauSac = getColorName(value)
}

const showAddColorModal = () => {
  newColorForm.tenMauSac = ''
  newColorForm.maMau = '#000000'
  addColorModalVisible.value = true
}

const closeAddColorModal = () => {
  addColorModalVisible.value = false
  newColorFormRef.value?.resetFields()
}

const handleAddColor = async () => {
  try {
    await newColorFormRef.value.validate()

    const newColor = {
      tenMauSac: newColorForm.tenMauSac,
      maMau: newColorForm.maMau,
      trangThai: true,
      deleted: false,
      createAt: new Date().toISOString().split('T')[0],
      createBy: userStore.id,
    }

    const response = await createMauSac(newColor)
    if (response.data) {
      // Reload inputs from API to get latest data
      await loadColorInputs()

      // Force Vue reactivity update and wait for DOM update
      await nextTick()

      // Additional timeout to ensure inputs are rendered
      setTimeout(() => {
        // Add to selected colors
        formData.colors.push(response.data)
      }, 100)

      closeAddColorModal()
      Message.success('Màu sắc đã được thêm thành công!')
    }
  } catch (error) {
    Message.error(`Lỗi khi thêm màu sắc: ${error.message || error}`)
  }
}

const showAddSizeModal = () => {
  newSizeForm.tenKichThuoc = ''
  addSizeModalVisible.value = true
}

const closeAddSizeModal = () => {
  addSizeModalVisible.value = false
  newSizeFormRef.value?.resetFields()
}

const handleAddSize = async () => {
  try {
    await newSizeFormRef.value.validate()

    const newSize = {
      tenKichThuoc: newSizeForm.tenKichThuoc,
      trangThai: true,
      deleted: false,
      createAt: new Date().toISOString().split('T')[0],
      createBy: userStore.id,
    }

    const response = await createKichThuoc(newSize)

    if (response.data) {
      // Reload inputs from API to get latest data
      await loadSizeInputs()

      // Force Vue reactivity update and wait for DOM update
      await nextTick()

      // Additional timeout to ensure inputs are rendered
      setTimeout(() => {
        // Add to selected sizes
        formData.sizes.push(response.data)
      }, 100)

      closeAddSizeModal()
      Message.success('Kích thước đã được thêm thành công!')
    }
  } catch (error) {
    Message.error(`Lỗi khi thêm kích thước: ${error.message || error}`)
  }
}

// Image management methods
const getColorImages = (colorId: string) => {
  return colorImages.value[colorId] || []
}

// Hiển thị modal quản lý ảnh cho màu sắc
const showImageModal = async (colorId: string) => {
  imageModalColor.value = colorId
  imageModalVisible.value = true

  // Reset selections
  selectedExistingImages.value = []
  newUploadImages.value = []
  loadingExistingImages.value = true

  try {
    // Lấy tên màu từ colorId để gọi API
    const colorInfo = colorInputs.value.find((c) => c.value === colorId)
    const colorName = colorInfo?.label || colorId

    // Gọi API để lấy ảnh có sẵn theo màu
    const response = await getAnhSanPhamByTenMau(colorName)
    existingImages.value = response.data || []
  } catch (error) {
    console.error('❌ Failed to load existing images:', error)
    existingImages.value = []
    Message.error('Không thể tải ảnh có sẵn')
  } finally {
    loadingExistingImages.value = false
  }
}

// Xử lý chọn ảnh có sẵn
const handleExistingImageSelect = (imageId: string) => {
  const index = selectedExistingImages.value.indexOf(imageId)
  if (index === -1) {
    selectedExistingImages.value.push(imageId)
  } else {
    selectedExistingImages.value.splice(index, 1)
  }
}

// Xử lý trước khi upload ảnh mới
const handleBeforeUpload = (file: File) => {
  const isValidType = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'].includes(file.type)
  if (!isValidType) {
    Message.error('Chỉ được upload file ảnh (JPG, PNG, GIF, WebP)!')
    return false
  }

  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isLt10M) {
    Message.error('Ảnh phải nhỏ hơn 10MB!')
    return false
  }

  // Tạo preview URL
  const fileWithUrl = file as any
  fileWithUrl.url = URL.createObjectURL(file)

  return false // Ngăn auto upload
}

// Đóng modal ảnh
const handleImageModalCancel = () => {
  imageModalVisible.value = false
  imageModalColor.value = null
  selectedExistingImages.value = []
  newUploadImages.value = []
}

// Xử lý OK modal ảnh - Upload ảnh mới và lưu IDs
const handleImageModalOk = async () => {
  uploadingImages.value = true
  uploadProgressText.value = 'Đang khởi tạo...'

  try {
    const uploadedImageIds: number[] = []

    // Upload ảnh mới nếu có
    if (newUploadImages.value.length > 0) {
      const files = newUploadImages.value.map((item) => item.originFile || item.file)

      const colorInfo = colorInputs.value.find((c) => {
        return c.value.toString() === imageModalColor.value?.toString()
      })
      const colorName = colorInfo?.label || 'Unknown Color'

      // Cập nhật trạng thái upload
      uploadProgressText.value = `Đang chuẩn bị upload ${files.length} ảnh cho màu ${colorName}...`

      // Hiển thị thông báo bắt đầu upload
      Message.info(`Đang upload ${files.length} ảnh cho màu ${colorName}...`)

      // Upload ảnh với timeout
      try {
        uploadProgressText.value = 'Đang upload ảnh...'
        // Gọi API upload với timeout
        const uploadResponse = await Promise.race([
          uploadMutipartFile(files, `${formData.name} - ${colorName}`, colorName, userStore.id),
          new Promise((_, reject) => {
            setTimeout(() => reject(new Error('Upload timeout after 30 seconds')), 30000)
          }),
        ])

        uploadProgressText.value = 'Hoàn thành upload!'

        if (uploadResponse.data && Array.isArray(uploadResponse.data)) {
          uploadedImageIds.push(...uploadResponse.data)
          Message.success(`Upload thành công ${uploadResponse.data.length} ảnh`)
        }

        // Thêm thời gian chờ sau khi upload ảnh để đảm bảo server xử lý xong
        uploadProgressText.value = 'Đang hoàn tất xử lý...'
        await new Promise((resolve) => {
          setTimeout(resolve, 2000)
        })
      } catch (uploadError) {
        uploadProgressText.value = 'Upload thất bại!'

        console.error('❌ Failed to upload images:', uploadError)
        if (uploadError.message.includes('timeout')) {
          Message.error('Upload ảnh quá thời gian cho phép (30s). Vui lòng thử lại.')
        } else {
          Message.error(`Lỗi upload ảnh: ${uploadError.message || 'Unknown error'}`)
        }
        uploadingImages.value = false
        return
      }
    }

    // Store uploaded image IDs for this color (bao gồm cả existing + new)
    const colorId = imageModalColor.value!
    if (!uploadedImageStore.value[colorId]) {
      uploadedImageStore.value[colorId] = []
    }

    // Thêm existing image IDs
    const existingImageIds = selectedExistingImages.value.map((id) => parseInt(id, 10))

    // Gộp existing + uploaded IDs
    const allImageIds = [...existingImageIds, ...uploadedImageIds]
    uploadedImageStore.value[colorId] = allImageIds

    // Combine selected existing images and new uploads
    const allImages = [
      ...selectedExistingImages.value.map((id) => {
        const image = existingImages.value.find((img) => img.id === id)
        return { id, url: image?.duongDanAnh, name: image?.tenAnh, type: 'existing' }
      }),
      ...newUploadImages.value.map((item) => ({
        url: item.url,
        name: item.name,
        type: 'new',
      })),
    ]

    // Store images for display
    colorImages.value[colorId] = allImages

    // Close modal
    handleImageModalCancel()
    Message.success(`Đã lưu ${allImages.length} ảnh cho màu này`)
  } catch (error) {
    console.error('❌ Failed to process images:', error)
    Message.error(`Lỗi xử lý ảnh: ${error.message || 'Unknown error'}`)
  } finally {
    uploadingImages.value = false
    uploadProgressText.value = ''
  }
}

// Xóa ảnh khỏi màu
const removeColorImage = (colorId: string, imageIndex: number) => {
  if (colorImages.value[colorId]) {
    colorImages.value[colorId].splice(imageIndex, 1)
  }
}

// Methods
const handleSubmit = async () => {
  try {
    await formRef.value.validate()

    // First, create the product if it's pending
    let productId = formData.selectedProductId
    if (productId === 'pending' && pendingNewProduct.value) {
      const productData = {
        tenSanPham: pendingNewProduct.value.tenSanPham,
        idNhaSanXuat: formData.manufacturer || 1, // Use selected manufacturer or default
        idXuatXu: formData.origin || 1, // Use selected origin or default
        trangThai: true,
        deleted: false,
        createAt: new Date().toISOString().split('T')[0],
        createBy: userStore.id,
      }
      const productResponse = await createDanhMucSanPham(productData)
      productId = productResponse.data.id
      // Clear pending state
      pendingNewProduct.value = null
    }

    // Validate required fields
    if (!formData.name.trim()) {
      Message.error('Vui lòng nhập tên sản phẩm')
      return
    }

    if (!productId) {
      Message.error('Vui lòng chọn sản phẩm từ danh sách có sẵn')
      return
    }

    if (variants.value.length === 0) {
      Message.error('Vui lòng chọn ít nhất một màu sắc và kích thước để tạo biến thể')
      return
    }

    // Validate all variants have required data
    const hasIncompleteVariant = variants.value.some((colorGroup) =>
      colorGroup.variants.some((variant) => !variant.price || variant.price <= 0 || variant.stock === undefined || variant.stock < 0)
    )

    if (hasIncompleteVariant) {
      Message.error('Vui lòng nhập đầy đủ giá bán và số lượng cho tất cả biến thể')
      return
    }

    loading.value = true

    // Sử dụng trực tiếp uploadedImageStore để lấy danh sách image IDs cho mỗi màu
    const finalImageIds = { ...uploadedImageStore.value }

    // Tạo danh sách promises cho việc tạo biến thể
    const variantPromises = variants.value.flatMap((colorGroup) =>
      colorGroup.variants.map(async (variant) => {
        const variantData = {
          idSanPham: productId,
          idMauSac: variant.color,
          idKichThuoc: variant.size,
          idDeGiay: formData.shoeSole || 1, // Default if not selected
          idChatLieu: formData.material || 1, // Default if not selected
          idTrongLuong: 1, // You might need to add weight selection
          soLuong: variant.stock || 0,
          giaBan: variant.price || 0,
          trangThai: true,
          deleted: false,
          createAt: new Date().toISOString().split('T')[0],
          createBy: userStore.id,
        }

        // Create the variant first - WAIT for completion before proceeding
        const variantResponse = await createBienTheSanPham(variantData)
        if (!variantResponse?.success || !variantResponse.data) {
          console.error('❌ Invalid variant response:', variantResponse)
          throw new Error('Failed to create variant - no ID returned')
        }

        const variantId = variantResponse.data // ID is directly in data field

        // NOW proceed with image linking - only after variant creation is complete
        const colorId = variant.color.toString()

        // Get all combined image IDs for this color using stored IDs
        const allImageIds = finalImageIds[colorId] || []

        if (allImageIds.length > 0) {
          // ONLY link images if we have a valid variant ID
          if (allImageIds.length > 0 && variantId) {
            try {
              const linkData = {
                idChiTietSanPham: variantId,
                idAnhSanPhamList: allImageIds,
                trangThai: true,
                deleted: false,
                createAt: new Date().toISOString().split('T')[0],
                createBy: userStore.id,
              }

              // WAIT for image linking to complete
              await themAnhChoBienThe(linkData)
            } catch (error) {
              console.error(`❌ Failed to link images to variant ${variantId}:`, error.message)
              console.error('🔍 Link error details:', error)
              // Don't stop the process for image linking errors, but log the error
            }
          }
        }

        // Return both response and variantId for collection
        return {
          response: variantResponse,
          variantId,
          sku: `${formData.name}-${variant.color}-${variant.size}`, // For identification
        }
      })
    )

    // Wait for all variants to be created and collect their IDs
    const variantResults = await Promise.all(variantPromises)
    const createdVariantIds = variantResults.map((result) => result.variantId).filter(Boolean)
    loading.value = false
    Message.success(`Biến thể sản phẩm đã được tạo thành công! (${createdVariantIds.length} biến thể)`)

    // Clear uploaded image store after successful submission
    uploadedImageStore.value = {}

    // Navigate to the product variants page for the selected product with created variant IDs
    router.push({
      name: 'BienTheSanPham',
      params: { id: productId },
      query: {
        newVariants: createdVariantIds.join(','),
        highlight: 'true',
      },
    })
  } catch (error) {
    loading.value = false
    console.error('❌ Submission failed:', error.message)
    Message.error(`Lỗi khi tạo biến thể sản phẩm: ${error.message || error}`)
  }
}

const handleCancel = () => {
  router.push({ name: 'DanhMucSanPham' })
}

// Load data on component mount
onMounted(() => {
  loadAllInputs()
})

// Watch for formData changes to update search values
watch(
  () => formData.manufacturer,
  (newVal) => {
    if (newVal && manufacturerInputs.value.length > 0) {
      const selectedInput = manufacturerInputs.value.find((input) => input.value === newVal)
      if (selectedInput && !manufacturerSearchValue.value) {
        manufacturerSearchValue.value = selectedInput.label
      }
    }
  }
)

watch(
  () => formData.origin,
  (newVal) => {
    if (newVal && originInputs.value.length > 0) {
      const selectedInput = originInputs.value.find((input) => input.value === newVal)
      if (selectedInput && !originSearchValue.value) {
        originSearchValue.value = selectedInput.label
      }
    }
  }
)

watch(
  () => formData.material,
  (newVal) => {
    if (newVal && materialInputs.value.length > 0) {
      const selectedInput = materialInputs.value.find((input) => input.value === newVal)
      if (selectedInput && !materialSearchValue.value) {
        materialSearchValue.value = selectedInput.label
      }
    }
  }
)

watch(
  () => formData.shoeSole,
  (newVal) => {
    if (newVal && shoeSoleInputs.value.length > 0) {
      const selectedInput = shoeSoleInputs.value.find((input) => input.value === newVal)
      if (selectedInput && !shoeSoleSearchValue.value) {
        shoeSoleSearchValue.value = selectedInput.label
      }
    }
  }
)
</script>

<style scoped>
.add-product-page {
  background-color: var(--color-fill-2);
  padding: 8px 20px;
  padding-bottom: 0;
}

.product-form-card {
  background-color: var(--color-bg-2);
  border-radius: 4px;
}

.product-form-card .arco-form-item-label {
  font-weight: 500;
  color: #1d2129;
  margin-bottom: 8px;
}

/* Product name autocomplete */
.product-name-autocomplete {
  position: relative;
  width: 100%;
}

.product-name-input {
  width: 100% !important;
}

.new-product-indicator {
  margin-top: 8px;
  padding: 4px 0;
}

.new-product-indicator .arco-tag {
  border-radius: 4px;
  font-size: 12px;
}

/* Search input containers */
.search-input-container {
  position: relative;
  width: 100%;
  display: flex;
  align-items: center;
  gap: 8px;
}

.search-input {
  flex: 1;
}

.input-add-btn {
  flex-shrink: 0;
  padding: 4px !important;
  border: 1px solid var(--color-border-2) !important;
  background: var(--color-bg-1) !important;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05) !important;
  color: var(--color-text-2);
  transition: all 0.3s ease;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  margin: 0 !important;
}

.input-add-btn:hover {
  background-color: var(--color-primary-light-1) !important;
  border-color: var(--color-primary-light-3) !important;
  color: var(--color-primary) !important;
  transform: scale(1.05);
  box-shadow: 0 2px 6px rgba(22, 93, 255, 0.15) !important;
}

.input-add-btn:active {
  transform: scale(0.95);
}

.input-add-btn .arco-icon {
  font-size: 14px;
}

.search-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  right: 40px; /* Leave space for the button */
  background: white;
  border: 1px solid var(--color-border-2);
  border-radius: 4px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
  max-height: 200px;
  overflow-y: auto;
  z-index: 1000;
  margin-top: 4px;
}

.search-option {
  padding: 8px 12px;
  cursor: pointer;
  border-bottom: 1px solid var(--color-border-1);
  transition: background-color 0.2s;
}

.search-option:hover {
  background-color: var(--color-fill-2);
}

.search-option:last-child {
  border-bottom: none;
}

.no-results {
  padding: 8px 12px;
  color: var(--color-text-3);
  text-align: center;
  font-style: italic;
}

.autocomplete-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: white;
  border: 1px solid var(--color-border-2);
  border-radius: 4px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
  max-height: 200px;
  overflow-y: auto;
  z-index: 1000;
}

.autocomplete-input {
  padding: 8px 12px;
  cursor: pointer;
  border-bottom: 1px solid var(--color-border-1);
}

.autocomplete-input:hover {
  background-color: var(--color-fill-2);
}

.autocomplete-input:last-child {
  border-bottom: none;
}

.input-name {
  font-weight: 500;
  color: var(--color-text-1);
}

.input-details {
  font-size: 12px;
  color: var(--color-text-3);
  margin-top: 2px;
}

.no-inputs {
  padding: 8px 12px;
  color: var(--color-text-3);
  text-align: center;
  font-style: italic;
}

.attribute-row {
  margin-bottom: 16px;
}

.attribute-selector {
  display: flex;
  align-items: flex-start;
  gap: 8px;
}

.select-attribute-btn {
  margin-bottom: 8px;
}

.selected-items {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  flex: 1;
}

.color-tag,
.size-tag {
  margin: 0;
}

/* Modal styles */
.attribute-modal-content {
  max-height: 400px;
  overflow-y: auto;
}

.attribute-inputs-list {
  display: flex;
  flex-direction: column;
}

.color-input-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.attribute-input-checkbox {
  margin: 0 0 8px 0;
  padding: 8px 12px;
  border-radius: 6px;
  border: 1px solid var(--color-border-2);
  transition: all 0.2s;
  background-color: var(--color-bg-2);
  flex: 1;
}

.attribute-input-checkbox:hover {
  background-color: var(--color-fill-1);
  border-color: var(--color-border-3);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

/* Active/checked state */
.attribute-input-checkbox[aria-checked='true'] {
  background-color: var(--color-primary-1);
  border-color: var(--color-primary-6);
  color: var(--color-primary-6);
  font-weight: 500;
  box-shadow: 0 2px 8px rgba(22, 93, 255, 0.15);
}

.attribute-input-checkbox[aria-checked='true']:hover {
  background-color: var(--color-primary-2);
  border-color: var(--color-primary-7);
  box-shadow: 0 4px 12px rgba(22, 93, 255, 0.25);
}

.color-preview-input {
  width: 40px;
  height: 40px;
  border: 1px solid var(--color-border-2);
  border-radius: 4px;
  margin-left: 12px;
  margin-bottom: 8px;
  cursor: pointer;
  background: none;
}

.color-preview-input:disabled {
  cursor: not-allowed;
  opacity: 1;
}

/* Variants Section - Now using a-card instead of custom styling */

.variants-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.variants-header h3 {
  margin: 0;
}

.variants-section h3 {
  margin: 0 0 16px 0;
  color: #1d2129;
  font-size: 16px;
  font-weight: 600;
}

.variants-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid var(--color-border-2);
}

.color-variant {
  margin-bottom: 24px;
}

.color-variant h4 {
  margin: 0 0 12px 0;
  color: #4e5969;
  font-size: 14px;
  font-weight: 500;
  padding-left: 8px;
  border-left: 3px solid #165dff;
}

.variant-table {
  margin-bottom: 16px;
}

.variant-table .arco-table-td {
  padding: 8px 12px;
}

/* Variant table pagination */
.variant-table .arco-pagination {
  margin-top: 12px;
  text-align: right;
}

.variant-table .arco-pagination .arco-pagination-total {
  margin-right: auto;
}

/* Image preview styles */
.image-preview {
  position: relative;
  display: inline-block;
}

.image-preview img {
  border-radius: 4px;
  border: 1px solid var(--color-border-2);
}

.image-count {
  position: absolute;
  top: -8px;
  right: -8px;
  background: var(--color-primary-6);
  color: white;
  border-radius: 50%;
  width: 16px;
  height: 16px;
  font-size: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 500;
}

.no-image {
  color: var(--color-text-3);
  font-size: 12px;
}

/* Image upload container */
.image-upload-container {
  display: flex;
  align-items: center;
}

.variant-image-upload {
  width: auto;
}

.variant-image-upload .arco-upload-list-picture-card .arco-upload-list-picture-card-container {
  width: 60px;
  height: 60px;
  margin-right: 8px;
  margin-bottom: 0;
}

.variant-image-upload .arco-upload-list-picture-card .arco-upload-list-item-picture {
  width: 60px;
  height: 60px;
}

.upload-button {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 60px;
  height: 60px;
  border: 1px dashed var(--color-border-2);
  border-radius: 4px;
  background-color: var(--color-fill-1);
  color: var(--color-text-3);
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s;
}

.upload-button:hover {
  border-color: var(--color-primary-6);
  color: var(--color-primary-6);
}

/* Attributes tooltip styles */
.attributes-tooltip {
  min-width: 200px;
}

.attribute-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 4px 0;
  border-bottom: 1px solid var(--color-border-2);
}

.attribute-item:last-child {
  border-bottom: none;
}

.attribute-label {
  font-weight: 500;
  color: var(--color-text-2);
  font-size: 12px;
  margin-right: 8px;
}

.attribute-value {
  font-weight: 600;
  color: var(--color-text-1);
  font-size: 12px;
}

.attributes-display {
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  color: var(--color-primary-6);
  font-weight: 500;
  font-size: 12px;
}

.primary-attr {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 80px;
}

.more-attrs {
  background-color: var(--color-primary-1);
  color: var(--color-primary-6);
  padding: 2px 6px;
  border-radius: 10px;
  font-size: 10px;
  font-weight: 600;
}

/* Attributes list styles */
.attributes-list {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.attribute-line {
  display: flex;
  align-items: center;
  font-size: 12px;
  line-height: 1.4;
}

.attr-label {
  font-weight: 500;
  color: var(--color-text-2);
  margin-right: 6px;
  min-width: 60px;
  white-space: nowrap;
}

.attr-value {
  font-weight: 600;
  color: var(--color-text-1);
  flex: 1;
}

/* Custom required field styling */
.required-field::after {
  content: ' *' !important;
  color: #f53f3f !important;
  font-weight: bold !important;
}

/* Label with add button styling */
.label-with-add-btn {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.label-with-add-btn .required-field {
  margin: 0;
  flex: 1;
}

.add-btn {
  margin-left: auto;
  justify-self: end;
  align-self: center;
  padding: 2px 6px !important;
  border-radius: 4px;
  border: 1px solid var(--color-border-2);
  background-color: var(--color-bg-1);
  transition: all 0.3s ease;
  opacity: 0.8;
  min-width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.add-btn:hover {
  opacity: 1;
  transform: scale(1.05);
  background-color: var(--color-primary-light-1);
  border-color: var(--color-primary-light-3);
  color: var(--color-primary);
  box-shadow: 0 2px 6px rgba(var(--primary-6), 0.15);
}

.add-btn:active {
  transform: scale(0.98);
  box-shadow: 0 1px 3px rgba(var(--primary-6), 0.2);
}

.add-btn:focus {
  outline: none;
  border-color: var(--color-primary-light-3);
  box-shadow: 0 0 0 2px rgba(var(--primary-6), 0.1);
}

.add-btn .arco-icon {
  font-size: 14px;
}

/* Action Buttons Card */
.action-buttons-card {
  background-color: var(--color-bg-2);
  border-radius: 4px;
}

.action-buttons-card .variants-actions {
  padding: 8px 0;
}

/* Image Management Styles */
.image-management-card {
  background-color: var(--color-bg-2);
  border-radius: 4px;
}

.color-image-grid {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.color-image-section {
  border: 1px solid var(--color-border-2);
  border-radius: 6px;
  padding: 16px;
  background-color: var(--color-bg-1);
}

.color-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.color-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.color-preview {
  width: 24px;
  height: 24px;
  border-radius: 4px;
  border: 1px solid var(--color-border-2);
}

.color-name {
  font-weight: 500;
  color: var(--color-text-1);
}

.no-images {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 40px;
  color: var(--color-text-3);
  background-color: var(--color-fill-2);
  border: 1px dashed var(--color-border-3);
  border-radius: 4px;
  text-align: center;
}

.image-thumbnails {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(80px, 1fr));
  gap: 8px;
}

.image-thumbnail {
  position: relative;
  width: 80px;
  height: 80px;
  border-radius: 4px;
  overflow: hidden;
  border: 1px solid var(--color-border-2);
}

.image-thumbnail img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-overlay {
  position: absolute;
  top: 4px;
  right: 4px;
  background-color: rgba(0, 0, 0, 0.5);
  border-radius: 2px;
  opacity: 0;
  transition: opacity 0.2s;
}

.image-thumbnail:hover .image-overlay {
  opacity: 1;
}

/* Modal Search Styles */
.search-section {
  margin-bottom: 16px;
}

.no-results {
  text-align: center;
  color: var(--color-text-3);
  padding: 20px;
  font-style: italic;
}

/* Image Modal Styles */
.image-modal-content {
  max-height: 500px;
  overflow-y: auto;
}

.existing-images-section h4 {
  margin-bottom: 12px;
  color: var(--color-text-1);
  font-weight: 500;
}

.no-existing-images {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  color: var(--color-text-3);
  background-color: var(--color-fill-2);
  border: 1px dashed var(--color-border-3);
  border-radius: 4px;
  margin-bottom: 16px;
}

.existing-images-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
  gap: 12px;
  margin-bottom: 16px;
}

.existing-image-item {
  position: relative;
  width: 100px;
  height: 100px;
  border-radius: 6px;
  overflow: hidden;
  border: 2px solid transparent;
  cursor: pointer;
  transition: all 0.2s;
}

.existing-image-item:hover {
  border-color: var(--color-primary-light-3);
}

.existing-image-item.selected {
  border-color: var(--color-primary-6);
  box-shadow: 0 0 0 2px rgba(var(--primary-6), 0.2);
}

.existing-image-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.existing-image-item .image-overlay {
  position: absolute;
  top: 4px;
  right: 4px;
  background-color: rgba(255, 255, 255, 0.9);
  border-radius: 2px;
  opacity: 1;
}

.image-check-overlay {
  position: absolute;
  top: 4px;
  right: 4px;
  width: 20px;
  height: 20px;
  background-color: var(--color-primary);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 12px;
}

.upload-new-section h4 {
  margin-bottom: 12px;
  color: var(--color-text-1);
  font-weight: 500;
}

.new-image-upload {
  width: 100%;
  margin-bottom: 12px;
}

.upload-button {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 16px;
  color: var(--color-text-3);
  border: 1px dashed var(--color-border-3);
  border-radius: 6px;
  background-color: var(--color-fill-1);
  transition: all 0.2s;
}

.upload-button:hover {
  border-color: var(--color-primary-light-3);
  color: var(--color-primary);
}

.upload-note {
  font-size: 12px;
  color: var(--color-text-3);
  line-height: 1.4;
}

.upload-note p {
  margin: 2px 0;
}

/* Responsive */
@media (max-width: 768px) {
  .add-product-page {
    padding: 16px;
  }

  .color-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .existing-images-grid {
    grid-template-columns: repeat(auto-fill, minmax(80px, 1fr));
  }

  .existing-image-item {
    width: 80px;
    height: 80px;
  }
}

/* Upload Loading Overlay */
.upload-loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 9999;
  backdrop-filter: blur(4px);
}

.upload-loading-content {
  background: white;
  padding: 40px;
  border-radius: 12px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.3);
  text-align: center;
  max-width: 400px;
  width: 90%;
}

.upload-loading-text {
  margin-top: 20px;
}

.upload-loading-text h3 {
  margin: 0 0 8px 0;
  color: #333;
  font-size: 18px;
  font-weight: 600;
}

.upload-loading-text p {
  margin: 0 0 20px 0;
  color: #666;
  font-size: 14px;
  line-height: 1.5;
}

.upload-loading-info {
  margin-top: 20px;
}

.loading-text {
  margin: 12px 0 0 0 !important;
  color: #1890ff;
  font-size: 13px;
  font-weight: 500;
}
</style>
