import React, { useState, useEffect, useRef } from 'react';
import api from '../../api/axiosConfig';

const FileViewer = ({ file, userId, onClose }) => {
  const [content, setContent] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [scale, setScale] = useState(100);
  const fileType = file?.fileType?.toLowerCase() || '';
  const fileName = file?.originalFileName || 'File';
  const fileId = file?.id;

  useEffect(() => {
    const fetchFile = async () => {
      try {
        setLoading(true);
        const response = await api.get(
          `/files/read/${fileId}${userId ? `?userId=${userId}` : ''}`,
          {
            responseType: 'blob',
          }
        );

        // Check file type
        if (fileType.includes('pdf')) {
          const url = URL.createObjectURL(response.data);
          setContent({ type: 'pdf', blob: response.data, url });
        } else if (
          fileType.includes('text') ||
          fileType.includes('plain') ||
          fileType.includes('json') ||
          fileType.includes('xml')
        ) {
          // Read text files
          const text = await response.data.text();
          setContent({ type: 'text', data: text });
        } else if (fileType.includes('image')) {
          const url = URL.createObjectURL(response.data);
          setContent({ type: 'image', url });
        } else if (
          fileType.includes('word') ||
          fileType.includes('document') ||
          fileType.includes('spreadsheet') ||
          fileType.includes('sheet')
        ) {
          // For Office documents, show preview with Google Docs Viewer
          const blob = response.data;
          const url = URL.createObjectURL(blob);
          setContent({ type: 'document', url });
        } else {
          // For unsupported types, show download option
          setContent({ type: 'unsupported', blob: response.data });
        }
        setError(null);
      } catch (err) {
        console.error('Error loading file:', err);
        console.error('Error response:', err.response?.data);
        console.error('Error status:', err.response?.status);
        const errorMsg = err.response?.data?.message || err.response?.data?.error || err.message || 'Unable to read file. The file may not be accessible or is in an unsupported format.';
        setError(errorMsg);
      } finally {
        setLoading(false);
      }
    };

    if (fileId) {
      fetchFile();
    }
    
    return () => {
      // Cleanup object URLs
      if (content?.url) {
        URL.revokeObjectURL(content.url);
      }
    };
  }, [fileId, fileType, userId]);

  const handleDownload = () => {
    if (content?.blob) {
      const url = URL.createObjectURL(content.blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = fileName;
      link.click();
      URL.revokeObjectURL(url);
    }
  };

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 z-50 flex items-center justify-center p-4">
      <div className="bg-white rounded-lg flex flex-col h-full max-h-[90vh] w-full max-w-4xl">
        {/* Header */}
        <div className="border-b border-gray-200 p-4 flex items-center justify-between">
          <div className="flex-1 min-w-0">
            <h2 className="text-xl font-semibold text-gray-800 truncate">
              {fileName}
            </h2>
          </div>
          <button
            onClick={onClose}
            className="ml-4 text-gray-400 hover:text-gray-600 transition-colors"
          >
            <svg
              className="w-6 h-6"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M6 18L18 6M6 6l12 12"
              />
            </svg>
          </button>
        </div>

        {/* Content Area */}
        <div className="flex-1 overflow-auto bg-gray-50">
          {loading && (
            <div className="flex items-center justify-center h-full">
              <div className="text-center">
                <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto mb-4"></div>
                <p className="text-gray-600">Loading file...</p>
              </div>
            </div>
          )}

          {error && (
            <div className="flex items-center justify-center h-full">
              <div className="text-center max-w-md">
                <div className="mb-4 text-red-600">
                  <svg
                    className="w-12 h-12 mx-auto"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2}
                      d="M12 8v4m0 4v.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
                    />
                  </svg>
                </div>
                <p className="text-gray-700 mb-4">{error}</p>
                <button
                  onClick={handleDownload}
                  className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700 transition-colors"
                >
                  Download Instead
                </button>
              </div>
            </div>
          )}

          {/* PDF Viewer */}
          {!loading && !error && content?.type === 'pdf' && (
            <div className="p-4 h-full flex flex-col">
              <div className="flex items-center justify-center gap-2 mb-4 bg-white p-3 rounded border border-gray-200">
                <button
                  onClick={() => setScale(Math.max(50, scale - 10))}
                  className="px-3 py-1 bg-gray-200 rounded hover:bg-gray-300 transition-colors text-sm"
                >
                  -
                </button>
                <span className="text-sm font-medium w-12 text-center">{scale}%</span>
                <button
                  onClick={() => setScale(Math.min(200, scale + 10))}
                  className="px-3 py-1 bg-gray-200 rounded hover:bg-gray-300 transition-colors text-sm"
                >
                  +
                </button>
                <button
                  onClick={handleDownload}
                  className="ml-auto px-4 py-1 bg-blue-600 text-white rounded hover:bg-blue-700 transition-colors text-sm"
                >
                  Download
                </button>
              </div>
              <div className="flex-1 flex items-center justify-center bg-gray-100 rounded overflow-auto">
                {content.url ? (
                  <iframe
                    src={content.url}
                    style={{ width: '100%', height: '100%', border: 'none' }}
                    title="PDF Viewer"
                  />
                ) : (
                  <div className="text-center p-4">
                    <p className="text-gray-600 mb-4">PDF viewing is supported via popular PDF readers</p>
                    <button
                      onClick={handleDownload}
                      className="bg-blue-600 text-white px-6 py-3 rounded hover:bg-blue-700 transition-colors"
                    >
                      Download PDF
                    </button>
                  </div>
                )}
              </div>
            </div>
          )}

          {/* Text Viewer */}
          {!loading && !error && content?.type === 'text' && (
            <pre className="p-4 font-mono text-sm text-gray-800 bg-white overflow-auto h-full whitespace-pre-wrap break-words">
              {content.data}
            </pre>
          )}

          {/* Image Viewer */}
          {!loading && !error && content?.type === 'image' && (
            <div className="flex items-center justify-center h-full p-4">
              <div className="flex flex-col items-center gap-4">
                <img
                  src={content.url}
                  alt={fileName}
                  style={{ maxWidth: '100%', maxHeight: '85%', objectFit: 'contain' }}
                  className="rounded"
                />
                <button
                  onClick={handleDownload}
                  className="bg-blue-600 text-white px-6 py-2 rounded hover:bg-blue-700 transition-colors"
                >
                  Download Image
                </button>
              </div>
            </div>
          )}

          {/* Document Viewer (Office files) */}
          {!loading && !error && content?.type === 'document' && (
            <div className="p-4 h-full flex flex-col">
              <div className="mb-4">
                <p className="text-gray-600 mb-3">
                  Preview unavailable for Office documents. Please download to view.
                </p>
              </div>
              <div className="flex-1 flex items-center justify-center">
                <button
                  onClick={handleDownload}
                  className="bg-blue-600 text-white px-6 py-3 rounded hover:bg-blue-700 transition-colors"
                >
                  Download {fileType.includes('word') ? 'Document' : 'File'}
                </button>
              </div>
            </div>
          )}

          {/* Unsupported Type */}
          {!loading && !error && content?.type === 'unsupported' && (
            <div className="flex items-center justify-center h-full">
              <div className="text-center">
                <p className="text-gray-600 mb-4">
                  File type not supported for preview.
                </p>
                <button
                  onClick={handleDownload}
                  className="bg-blue-600 text-white px-6 py-3 rounded hover:bg-blue-700 transition-colors"
                >
                  Download File
                </button>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default FileViewer;
