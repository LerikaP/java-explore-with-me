package ru.practicum.category.service;

import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.util.CustomPageRequest;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.category.mapper.CategoryMapper;
import ru.practicum.category.model.CategoryEntity;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.event.model.QEventEntity;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.ValidationException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.UniquenessViolationException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public List<CategoryDto> getCategories(int from, int size) {
        PageRequest pageRequest = new CustomPageRequest(from, size, Sort.by(Sort.Direction.DESC, "id"));
        return categoryRepository.findAll(pageRequest)
                .stream()
                .map(categoryMapper::toCategoryDto)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryDto getCategory(long catId) {
        return categoryMapper.toCategoryDto(getCategoryById(catId));
    }

    @Transactional
    @Override
    public CategoryDto createCategory(NewCategoryDto newCategoryDto) {
        try {
            return categoryMapper.toCategoryDto(categoryRepository.save(categoryMapper.toCategory(newCategoryDto)));
        } catch (DataIntegrityViolationException e) {
            throw new UniquenessViolationException(
                    String.format("Category with name %s already exists", newCategoryDto.getName()));
        }
    }

    @Transactional
    @Override
    public CategoryDto updateCategory(NewCategoryDto newCategoryDto, long catId) {
        CategoryEntity category = getCategoryById(catId);
        category.setName(newCategoryDto.getName());
        try {
            return categoryMapper.toCategoryDto(categoryRepository.save(category));
        } catch (DataIntegrityViolationException e) {
            throw new UniquenessViolationException(
                    String.format("Category with name %s already exists", newCategoryDto.getName()));
        }
    }

    @Transactional
    @Override
    public void deleteCategory(long catId) {
        getCategoryById(catId);
        BooleanExpression selectById = QEventEntity.eventEntity.category.id.in(catId);
        if (eventRepository.count(selectById) > 0) {
            throw new ValidationException(String.format("There are events associated with category with id %s", catId));
        }
        categoryRepository.deleteById(catId);
    }

    private CategoryEntity getCategoryById(long catId) {
        return categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException(String.format("Category with id %s was not found", catId)));
    }
}
